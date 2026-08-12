package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.MuseumViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MuseumViewHistoryRepository extends JpaRepository<MuseumViewHistory, Long> {
    Optional<MuseumViewHistory> findByArtistIdAndMuseumId(Long artistId, Long museumId);
    List<MuseumViewHistory> findTop30ByArtistIdOrderByViewedAtDesc(Long artistId);
}
