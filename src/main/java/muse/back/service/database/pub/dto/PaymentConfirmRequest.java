package muse.back.service.database.pub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PaymentConfirmRequest(
        @NotBlank @Size(max = 200) String paymentKey,
        @NotBlank @Size(min = 6, max = 64) @Pattern(regexp = "[A-Za-z0-9_-]+") String orderId,
        @Positive int amount
) {}
