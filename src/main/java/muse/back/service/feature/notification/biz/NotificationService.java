package muse.back.service.feature.notification.biz;

import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.NotificationListResponse;
import muse.back.service.database.pub.dto.NotificationResponse;
import muse.back.service.database.pub.entity.MuseNotification;
import muse.back.service.database.pub.repository.MuseNotificationRepository;
import muse.back.service.feature.profile.biz.ArtistIdentityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final MuseNotificationRepository notificationRepository;
    private final ArtistIdentityService artistIdentityService;

    public NotificationListResponse getMine(String userKey) {
        Long artistId = artistIdentityService.requireByUserKey(userKey).getArtistId();
        return new NotificationListResponse(
                notificationRepository.countByArtistIdAndReadAtIsNull(artistId),
                notificationRepository.findTop100ByArtistIdOrderByNotificationIdDesc(artistId)
                        .stream().map(this::toResponse).toList()
        );
    }

    @Transactional
    public NotificationResponse markRead(String userKey, Long notificationId) {
        Long artistId = artistIdentityService.requireByUserKey(userKey).getArtistId();
        MuseNotification notification = notificationRepository
                .findByNotificationIdAndArtistId(notificationId, artistId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Notification not found"));
        notification.markRead(LocalDateTime.now());
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public long markAllRead(String userKey) {
        Long artistId = artistIdentityService.requireByUserKey(userKey).getArtistId();
        var notifications = notificationRepository.findByArtistIdAndReadAtIsNull(artistId);
        LocalDateTime now = LocalDateTime.now();
        notifications.forEach(item -> item.markRead(now));
        notificationRepository.saveAll(notifications);
        return notifications.size();
    }

    @Transactional
    public void create(Long artistId, String type, String title, String message, String href, String dedupeKey) {
        if (dedupeKey != null && notificationRepository.existsByArtistIdAndDedupeKey(artistId, dedupeKey)) return;
        notificationRepository.save(new MuseNotification(artistId, type, title, message, href, dedupeKey));
    }

    private NotificationResponse toResponse(MuseNotification notification) {
        return new NotificationResponse(
                notification.getNotificationId(), notification.getNotificationType(), notification.getTitle(),
                notification.getMessage(), notification.getHref(), notification.getReadAt() != null,
                notification.getCreatedAt()
        );
    }
}
