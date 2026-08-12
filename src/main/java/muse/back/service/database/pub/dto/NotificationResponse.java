package muse.back.service.database.pub.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long notificationId,
        String type,
        String title,
        String message,
        String href,
        boolean read,
        LocalDateTime createdAt
) {}
