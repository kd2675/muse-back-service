package muse.back.service.feature.payment.act;

import auth.common.core.context.RequirePrincipalRole;
import auth.common.core.context.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.PaymentCancelRequest;
import muse.back.service.database.pub.dto.PaymentConfirmRequest;
import muse.back.service.database.pub.dto.PaymentOrderCreateRequest;
import muse.back.service.database.pub.dto.PaymentOrderResponse;
import muse.back.service.database.pub.dto.PaymentWebhookRequest;
import muse.back.service.feature.payment.biz.PaymentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.core.response.base.dto.ResponseDataDTO;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/api/muse/v1/me/payments/orders")
    @RequirePrincipalRole
    public ResponseDataDTO<PaymentOrderResponse> createOrder(
            @Valid @RequestBody PaymentOrderCreateRequest request,
            UserContext context
    ) {
        return ResponseDataDTO.of(paymentService.createOrder(requireUserKey(context), request), "결제 주문 생성 성공");
    }

    @PostMapping("/api/muse/v1/me/payments/confirm")
    @RequirePrincipalRole
    public ResponseDataDTO<PaymentOrderResponse> confirm(
            @Valid @RequestBody PaymentConfirmRequest request,
            UserContext context
    ) {
        return ResponseDataDTO.of(paymentService.confirm(requireUserKey(context), request), "결제 승인 성공");
    }

    @GetMapping("/api/muse/v1/me/payments")
    @RequirePrincipalRole
    public ResponseDataDTO<List<PaymentOrderResponse>> getMine(UserContext context) {
        return ResponseDataDTO.of(paymentService.getMine(requireUserKey(context)), "결제 내역 조회 성공");
    }

    @PostMapping("/api/muse/v1/me/payments/{orderId}/cancel")
    @RequirePrincipalRole
    public ResponseDataDTO<PaymentOrderResponse> cancel(
            @PathVariable String orderId,
            @Valid @RequestBody PaymentCancelRequest request,
            UserContext context
    ) {
        return ResponseDataDTO.of(
                paymentService.cancel(requireUserKey(context), orderId, request),
                "결제 취소 성공"
        );
    }

    @PostMapping("/api/muse/v1/payments/webhooks/toss")
    public ResponseDataDTO<Void> webhook(@Valid @RequestBody PaymentWebhookRequest request) {
        paymentService.reconcileWebhook(request.data().paymentKey());
        return ResponseDataDTO.of(null, "결제 웹훅 처리 성공");
    }

    private String requireUserKey(UserContext context) {
        if (context == null || context.getUserKey() == null || context.getUserKey().isBlank()) {
            throw new GeneralException(Code.UNAUTHORIZED, "Login required");
        }
        return context.getUserKey();
    }
}
