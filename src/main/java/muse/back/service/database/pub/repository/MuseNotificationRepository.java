package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.MuseNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MuseNotificationRepository extends JpaRepository<MuseNotification, Long> {
    List<MuseNotification> findTop100ByArtistIdOrderByNotificationIdDesc(Long artistId);
    long countByArtistIdAndReadAtIsNull(Long artistId);
    Optional<MuseNotification> findByNotificationIdAndArtistId(Long notificationId, Long artistId);
    List<MuseNotification> findByArtistIdAndReadAtIsNull(Long artistId);
    boolean existsByArtistIdAndDedupeKey(Long artistId, String dedupeKey);
}
