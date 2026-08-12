package muse.back.service.database.pub.dto;

import java.util.List;

public record NotificationListResponse(long unreadCount, List<NotificationResponse> items) {}
