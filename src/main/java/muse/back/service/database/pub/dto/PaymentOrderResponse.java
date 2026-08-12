package muse.back.service.database.pub.dto;

import java.time.LocalDateTime;

public record PaymentOrderResponse(
        String orderId,
        Long contestId,
        String orderName,
        int amount,
        String status,
        String clientKey,
        String customerKey,
        String receiptUrl,
        LocalDateTime createdAt
) {}
