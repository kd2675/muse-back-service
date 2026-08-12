package muse.back.service.database.pub.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PaymentWebhookRequest(String eventType, @NotNull @Valid Data data) {
    public record Data(
            @NotBlank @Size(max = 200) String paymentKey,
            String orderId,
            String status,
            int totalAmount
    ) {}
}
