package muse.back.service.feature.payment.biz;

import muse.back.service.database.pub.dto.ContestDetailResponse;
import muse.back.service.database.pub.dto.PaymentConfirmRequest;
import muse.back.service.database.pub.dto.PaymentOrderCreateRequest;
import muse.back.service.database.pub.entity.PaymentOrder;
import muse.back.service.database.pub.entity.ProfileArtist;
import muse.back.service.database.pub.repository.PaymentOrderRepository;
import muse.back.service.feature.contest.biz.ContestService;
import muse.back.service.feature.notification.biz.NotificationService;
import muse.back.service.feature.profile.biz.ArtistIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import web.common.core.response.base.exception.GeneralException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock private PaymentOrderRepository paymentOrderRepository;
    @Mock private ArtistIdentityService artistIdentityService;
    @Mock private ContestService contestService;
    @Mock private TossPaymentClient tossPaymentClient;
    @Mock private NotificationService notificationService;
    @InjectMocks private PaymentService paymentService;

    @Test
    void confirm_amountDoesNotMatchStoredOrder_rejectsBeforeProviderCall() {
        PaymentOrder order = new PaymentOrder("MUSE-1-order", 10L, 1L, "출품권", 10_000, "idem");
        when(artistIdentityService.requireByUserKey("user-1"))
                .thenReturn(new ProfileArtist(10L, "user-1", "Artist", null, "#111111"));
        when(paymentOrderRepository.findByOrderIdForUpdate("MUSE-1-order")).thenReturn(Optional.of(order));

        assertThrows(
                GeneralException.class,
                () -> paymentService.confirm("user-1", new PaymentConfirmRequest("payment-key", "MUSE-1-order", 1))
        );

        verify(tossPaymentClient, never()).confirm(any(), any(), any(Integer.class), any());
    }

    @Test
    void confirm_providerDataMatches_grantsCreditOnceAndReturnsDone() {
        PaymentOrder order = new PaymentOrder("MUSE-1-order", 10L, 1L, "출품권", 10_000, "idem");
        when(artistIdentityService.requireByUserKey("user-1"))
                .thenReturn(new ProfileArtist(10L, "user-1", "Artist", null, "#111111"));
        when(paymentOrderRepository.findByOrderIdForUpdate("MUSE-1-order")).thenReturn(Optional.of(order));
        when(tossPaymentClient.confirm("payment-key", "MUSE-1-order", 10_000, "idem"))
                .thenReturn(new TossPaymentClient.PaymentData(
                        "payment-key", "MUSE-1-order", "출품권", "DONE", 10_000,
                        "2026-08-12T16:30:00+09:00", "secret", new TossPaymentClient.PaymentData.Receipt("https://receipt")
                ));

        var response = paymentService.confirm(
                "user-1",
                new PaymentConfirmRequest("payment-key", "MUSE-1-order", 10_000)
        );

        assertThat(response.status()).isEqualTo("DONE");
        assertThat(response.receiptUrl()).isEqualTo("https://receipt");
        verify(contestService).grantPaidEntryCredit(1L, 10L, "MUSE-1-order");
        verify(notificationService).create(
                10L, "PAYMENT_DONE", "출품권 결제가 완료되었습니다",
                "출품권 1개가 지급되었습니다.", "/contest/1", "PAYMENT_DONE:MUSE-1-order"
        );
    }

    @Test
    void reconcileWebhook_donePayment_createsCompletionNotification() {
        PaymentOrder order = new PaymentOrder("MUSE-1-order", 10L, 1L, "출품권", 10_000, "idem");
        when(tossPaymentClient.get("payment-key")).thenReturn(new TossPaymentClient.PaymentData(
                "payment-key", "MUSE-1-order", "출품권", "DONE", 10_000,
                "2026-08-12T16:30:00+09:00", "secret", null
        ));
        when(paymentOrderRepository.findByOrderIdForUpdate("MUSE-1-order")).thenReturn(Optional.of(order));

        paymentService.reconcileWebhook("payment-key");

        verify(notificationService).create(
                10L, "PAYMENT_DONE", "출품권 결제가 완료되었습니다",
                "출품권 1개가 지급되었습니다.", "/contest/1", "PAYMENT_DONE:MUSE-1-order"
        );
    }

    @Test
    void createOrder_longContestTheme_limitsProviderOrderNameToOneHundredCharacters() {
        when(tossPaymentClient.requireClientKey()).thenReturn("client-key");
        when(artistIdentityService.requireByUserKey("user-1"))
                .thenReturn(new ProfileArtist(10L, "user-1", "Artist", null, "#111111"));
        when(contestService.getContestDetail(1L)).thenReturn(new ContestDetailResponse(
                1L, "가".repeat(200), null, "2026.08", 10_000, 100_000, 1, "SUBMISSION",
                null, null, null, null, 0, List.of("rule")
        ));
        when(paymentOrderRepository.saveAndFlush(any(PaymentOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = paymentService.createOrder("user-1", new PaymentOrderCreateRequest(1L));

        assertThat(response.orderName()).hasSize(100);
    }
}
