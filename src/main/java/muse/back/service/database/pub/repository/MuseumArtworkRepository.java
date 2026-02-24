package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.MuseumArtwork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MuseumArtworkRepository extends JpaRepository<MuseumArtwork, Long> {

    List<MuseumArtwork> findByMuseumIdOrderByMuseumArtworkIdDesc(Long museumId);

    List<MuseumArtwork> findByMuseumIdAndModerationStatusOrderByMuseumArtworkIdDesc(
            Long museumId,
            String moderationStatus
    );

    long countByMuseumId(Long museumId);

    long countByMuseumIdAndModerationStatus(Long museumId, String moderationStatus);

    Optional<MuseumArtwork> findByMuseumArtworkIdAndMuseumId(Long museumArtworkId, Long museumId);

    Optional<MuseumArtwork> findByMuseumArtworkIdAndArtistId(Long museumArtworkId, Long artistId);

    void deleteByMuseumId(Long museumId);
}
