package muse.back.service.feature.notification.act;

import auth.common.core.context.RequirePrincipalRole;
import auth.common.core.context.UserContext;
import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.NotificationListResponse;
import muse.back.service.database.pub.dto.NotificationResponse;
import muse.back.service.feature.notification.biz.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.core.response.base.dto.ResponseDataDTO;

@RestController
@RequirePrincipalRole
@RequestMapping("/api/muse/v1/me/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseDataDTO<NotificationListResponse> getMine(UserContext context) {
        return ResponseDataDTO.of(notificationService.getMine(context.getUserKey()), "알림 조회 성공");
    }

    @PutMapping("/{notificationId}/read")
    public ResponseDataDTO<NotificationResponse> markRead(@PathVariable Long notificationId, UserContext context) {
        return ResponseDataDTO.of(notificationService.markRead(context.getUserKey(), notificationId), "알림 읽음 처리 성공");
    }

    @PutMapping("/read-all")
    public ResponseDataDTO<Long> markAllRead(UserContext context) {
        return ResponseDataDTO.of(notificationService.markAllRead(context.getUserKey()), "모든 알림 읽음 처리 성공");
    }
}
