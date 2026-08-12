package muse.back.service.feature.payment.biz;

import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.ContestDetailResponse;
import muse.back.service.database.pub.dto.PaymentCancelRequest;
import muse.back.service.database.pub.dto.PaymentConfirmRequest;
import muse.back.service.database.pub.dto.PaymentOrderCreateRequest;
import muse.back.service.database.pub.dto.PaymentOrderResponse;
import muse.back.service.database.pub.entity.PaymentOrder;
import muse.back.service.database.pub.repository.PaymentOrderRepository;
import muse.back.service.feature.contest.biz.ContestService;
import muse.back.service.feature.profile.biz.ArtistIdentityService;
import muse.back.service.feature.notification.biz.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private final PaymentOrderRepository paymentOrderRepository;
    private final ArtistIdentityService artistIdentityService;
    private final ContestService contestService;
    private final TossPaymentClient tossPaymentClient;
    private final NotificationService notificationService;

    @Transactional
    public PaymentOrderResponse createOrder(String userKey, PaymentOrderCreateRequest request) {
        if (request == null || request.contestId() == null) {
            throw new GeneralException(Code.VALIDATION_ERROR, "contestId is required");
        }
        tossPaymentClient.requireClientKey();
        var artist = artistIdentityService.requireByUserKey(userKey);
        ContestDetailResponse contest = contestService.getContestDetail(request.contestId());
        if (!"SUBMISSION".equals(contest.phase())) {
            throw new GeneralException(Code.CONFLICT, "Payment is available only during submission period");
        }
        if (contest.entryFee() <= 0) {
            throw new GeneralException(Code.CONFLICT, "Contest entry fee is invalid");
        }
        String orderId = "MUSE-" + request.contestId() + "-" + UUID.randomUUID().toString().replace("-", "");
        PaymentOrder order = paymentOrderRepository.saveAndFlush(new PaymentOrder(
                orderId,
                artist.getArtistId(),
                request.contestId(),
                orderName(contest.theme()),
                contest.entryFee(),
                UUID.randomUUID().toString()
        ));
        return toResponse(order, customerKey(userKey));
    }

    @Transactional
    public PaymentOrderResponse confirm(String userKey, PaymentConfirmRequest request) {
        if (request == null) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Request body is required");
        }
        Long artistId = artistIdentityService.requireByUserKey(userKey).getArtistId();
        PaymentOrder order = paymentOrderRepository.findByOrderIdForUpdate(request.orderId())
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Payment order not found"));
        requireOwner(order, artistId);
        if (order.getAmount() != request.amount()) {
            throw new GeneralException(Code.CONFLICT, "Payment amount does not match stored order");
        }
        if ("DONE".equals(order.getStatus())) {
            if (!request.paymentKey().equals(order.getPaymentKey())) {
                throw new GeneralException(Code.CONFLICT, "Payment key does not match completed order");
            }
            return toResponse(order, customerKey(userKey));
        }
        if (!"READY".equals(order.getStatus()) && !"FAILED".equals(order.getStatus())) {
            throw new GeneralException(Code.CONFLICT, "Payment order cannot be confirmed in current state");
        }

        TossPaymentClient.PaymentData payment = tossPaymentClient.confirm(
                request.paymentKey(), request.orderId(), request.amount(), order.getIdempotencyKey()
        );
        validateProviderPayment(order, payment, "DONE");
        order.markPaid(
                payment.paymentKey(), payment.secret(), receiptUrl(payment), parseProviderTime(payment.approvedAt())
        );
        paymentOrderRepository.save(order);
        contestService.grantPaidEntryCredit(order.getContestId(), artistId, order.getOrderId());
        notifyPaymentDone(order);
        return toResponse(order, customerKey(userKey));
    }

    public List<PaymentOrderResponse> getMine(String userKey) {
        Long artistId = artistIdentityService.requireByUserKey(userKey).getArtistId();
        String customerKey = customerKey(userKey);
        return paymentOrderRepository.findTop50ByArtistIdOrderByPaymentOrderIdDesc(artistId)
                .stream().map(order -> toResponse(order, customerKey)).toList();
    }

    @Transactional
    public PaymentOrderResponse cancel(String userKey, String orderId, PaymentCancelRequest request) {
        if (request == null || request.reason() == null || request.reason().isBlank()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Cancellation reason is required");
        }
        Long artistId = artistIdentityService.requireByUserKey(userKey).getArtistId();
        PaymentOrder order = paymentOrderRepository.findByOrderIdForUpdate(orderId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Payment order not found"));
        requireOwner(order, artistId);
        if ("CANCELED".equals(order.getStatus())) return toResponse(order, customerKey(userKey));
        if (!"DONE".equals(order.getStatus()) || order.getPaymentKey() == null) {
            throw new GeneralException(Code.CONFLICT, "Only completed payments can be canceled");
        }
        contestService.revokePaidEntryCredit(order.getContestId(), artistId, orderId);
        TossPaymentClient.PaymentData payment = tossPaymentClient.cancel(
                order.getPaymentKey(), request.reason().trim(), "CANCEL-" + order.getIdempotencyKey()
        );
        if (!orderId.equals(payment.orderId())
                || order.getAmount() != payment.totalAmount()
                || !"CANCELED".equals(payment.status())) {
            throw new GeneralException(Code.BAD_GATEWAY, "Canceled provider payment does not match stored order");
        }
        order.markCanceled(LocalDateTime.now(SERVICE_ZONE));
        PaymentOrder saved = paymentOrderRepository.save(order);
        notifyCancellation(saved);
        return toResponse(saved, customerKey(userKey));
    }

    @Transactional
    public void reconcileWebhook(String paymentKey) {
        TossPaymentClient.PaymentData payment = tossPaymentClient.get(paymentKey);
        PaymentOrder order = paymentOrderRepository.findByOrderIdForUpdate(payment.orderId())
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Payment order not found"));
        if (order.getAmount() != payment.totalAmount()) {
            throw new GeneralException(Code.CONFLICT, "Webhook payment amount does not match stored order");
        }
        if ("DONE".equals(payment.status())) {
            order.markPaid(payment.paymentKey(), payment.secret(), receiptUrl(payment), parseProviderTime(payment.approvedAt()));
            paymentOrderRepository.save(order);
            contestService.grantPaidEntryCredit(order.getContestId(), order.getArtistId(), order.getOrderId());
            notifyPaymentDone(order);
        } else if (payment.status() != null && payment.status().startsWith("CANCELED")) {
            contestService.revokePaidEntryCredit(order.getContestId(), order.getArtistId(), order.getOrderId());
            order.markCanceled(LocalDateTime.now(SERVICE_ZONE));
            paymentOrderRepository.save(order);
            notifyCancellation(order);
        }
    }

    private void validateProviderPayment(PaymentOrder order, TossPaymentClient.PaymentData payment, String status) {
        if (!order.getOrderId().equals(payment.orderId())
                || order.getAmount() != payment.totalAmount()
                || !status.equals(payment.status())) {
            throw new GeneralException(Code.CONFLICT, "Payment provider data does not match stored order");
        }
    }

    private void requireOwner(PaymentOrder order, Long artistId) {
        if (!artistId.equals(order.getArtistId())) {
            throw new GeneralException(Code.NOT_FOUND, "Payment order not found");
        }
    }

    private PaymentOrderResponse toResponse(PaymentOrder order, String customerKey) {
        return new PaymentOrderResponse(
                order.getOrderId(), order.getContestId(), order.getOrderName(), order.getAmount(),
                order.getStatus(), tossPaymentClient.clientKeyOrNull(), customerKey,
                order.getReceiptUrl(), order.getCreatedAt()
        );
    }

    private String customerKey(String userKey) {
        return "muse_" + UUID.nameUUIDFromBytes(userKey.getBytes(StandardCharsets.UTF_8));
    }

    private String orderName(String theme) {
        String value = theme + " 출품권";
        int end = value.offsetByCodePoints(0, Math.min(100, value.codePointCount(0, value.length())));
        return value.substring(0, end);
    }

    private String receiptUrl(TossPaymentClient.PaymentData payment) {
        return payment.receipt() == null ? null : payment.receipt().url();
    }

    private void notifyCancellation(PaymentOrder order) {
        notificationService.create(
                order.getArtistId(),
                "PAYMENT_CANCELED",
                "출품권 결제가 취소되었습니다",
                order.getOrderName() + " 결제 취소와 출품권 회수가 완료되었습니다.",
                "/library",
                "PAYMENT_CANCELED:" + order.getOrderId()
        );
    }

    private void notifyPaymentDone(PaymentOrder order) {
        notificationService.create(
                order.getArtistId(),
                "PAYMENT_DONE",
                "출품권 결제가 완료되었습니다",
                order.getOrderName() + " 1개가 지급되었습니다.",
                "/contest/" + order.getContestId(),
                "PAYMENT_DONE:" + order.getOrderId()
        );
    }

    private LocalDateTime parseProviderTime(String value) {
        if (value == null || value.isBlank()) return LocalDateTime.now(SERVICE_ZONE);
        return OffsetDateTime.parse(value).atZoneSameInstant(SERVICE_ZONE).toLocalDateTime();
    }
}
