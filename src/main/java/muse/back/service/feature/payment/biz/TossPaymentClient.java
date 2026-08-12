package muse.back.service.feature.payment.biz;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriUtils;
import lombok.extern.slf4j.Slf4j;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@Slf4j
public class TossPaymentClient {
    private final boolean enabled;
    private final String clientKey;
    private final String secretKey;
    private final RestClient restClient;

    public TossPaymentClient(
            @Value("${muse.payment.toss.enabled:false}") boolean enabled,
            @Value("${muse.payment.toss.client-key:}") String clientKey,
            @Value("${muse.payment.toss.secret-key:}") String secretKey,
            @Value("${muse.payment.toss.api-base-url:https://api.tosspayments.com}") String apiBaseUrl
    ) {
        this.enabled = enabled;
        this.clientKey = clientKey;
        this.secretKey = secretKey;
        String credentials = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        this.restClient = RestClient.builder()
                .baseUrl(apiBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .build();
    }

    public String requireClientKey() {
        requireConfigured();
        return clientKey;
    }

    public String clientKeyOrNull() {
        return enabled && clientKey != null && !clientKey.isBlank() ? clientKey : null;
    }

    public PaymentData confirm(String paymentKey, String orderId, int amount, String idempotencyKey) {
        requireConfigured();
        return post(
                "/v1/payments/confirm",
                new ConfirmBody(paymentKey, orderId, amount),
                idempotencyKey
        );
    }

    public PaymentData cancel(String paymentKey, String reason, String idempotencyKey) {
        requireConfigured();
        return post(
                paymentPath(paymentKey) + "/cancel",
                new CancelBody(reason),
                idempotencyKey
        );
    }

    public PaymentData get(String paymentKey) {
        requireConfigured();
        try {
            PaymentData response = restClient.get().uri(paymentPath(paymentKey))
                    .retrieve().body(PaymentData.class);
            return requireValid(response);
        } catch (RestClientResponseException exception) {
            throw providerException(exception);
        } catch (GeneralException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new GeneralException(Code.BAD_GATEWAY, "Unable to verify payment with provider");
        }
    }

    private PaymentData post(String path, Object body, String idempotencyKey) {
        try {
            PaymentData response = restClient.post()
                    .uri(path)
                    .header("Idempotency-Key", idempotencyKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(PaymentData.class);
            return requireValid(response);
        } catch (RestClientResponseException exception) {
            throw providerException(exception);
        } catch (GeneralException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new GeneralException(Code.BAD_GATEWAY, "Payment provider request failed");
        }
    }

    private PaymentData requireValid(PaymentData response) {
        if (response == null || response.paymentKey() == null || response.orderId() == null) {
            throw new GeneralException(Code.BAD_GATEWAY, "Payment provider response is invalid");
        }
        return response;
    }

    private String paymentPath(String paymentKey) {
        return "/v1/payments/" + UriUtils.encodePathSegment(paymentKey, StandardCharsets.UTF_8);
    }

    private GeneralException providerException(RestClientResponseException exception) {
        String body = exception.getResponseBodyAsString();
        log.warn(
                "Toss Payments request rejected: status={}, response={}",
                exception.getStatusCode(),
                body == null ? "" : body.substring(0, Math.min(body.length(), 400))
        );
        return new GeneralException(
                exception.getStatusCode().is4xxClientError() ? Code.BAD_REQUEST : Code.BAD_GATEWAY,
                "Payment provider rejected the request"
        );
    }

    private void requireConfigured() {
        if (!enabled || clientKey == null || clientKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            throw new GeneralException(Code.SERVER_DOWN, "Payment provider is not configured");
        }
    }

    private record ConfirmBody(String paymentKey, String orderId, int amount) {}
    private record CancelBody(String cancelReason) {}

    public record PaymentData(
            String paymentKey,
            String orderId,
            String orderName,
            String status,
            int totalAmount,
            String approvedAt,
            String secret,
            Receipt receipt
    ) {
        public record Receipt(String url) {}
    }
}
