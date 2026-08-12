package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.MuseumBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MuseumBookmarkRepository extends JpaRepository<MuseumBookmark, Long> {
    Optional<MuseumBookmark> findByArtistIdAndMuseumId(Long artistId, Long museumId);
    boolean existsByArtistIdAndMuseumId(Long artistId, Long museumId);
    List<MuseumBookmark> findByArtistIdOrderByMuseumBookmarkIdDesc(Long artistId);
}
