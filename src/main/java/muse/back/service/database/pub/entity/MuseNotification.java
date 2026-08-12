package muse.back.service.database.pub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muse.back.service.common.jpa.CommonDateEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "muse_notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MuseNotification extends CommonDateEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;
    @Column(name = "artist_id", nullable = false)
    private Long artistId;
    @Column(name = "notification_type", length = 40, nullable = false)
    private String notificationType;
    @Column(name = "title", length = 160, nullable = false)
    private String title;
    @Column(name = "message", length = 500, nullable = false)
    private String message;
    @Column(name = "href", length = 500)
    private String href;
    @Column(name = "dedupe_key", length = 160)
    private String dedupeKey;
    @Column(name = "read_at")
    private LocalDateTime readAt;

    public MuseNotification(Long artistId, String notificationType, String title, String message, String href, String dedupeKey) {
        this.artistId = artistId;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
        this.href = href;
        this.dedupeKey = dedupeKey;
    }

    public void markRead(LocalDateTime value) {
        if (readAt == null) readAt = value;
    }
}
