package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.GalleryHighlight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GalleryHighlightRepository extends JpaRepository<GalleryHighlight, Long> {

    List<GalleryHighlight> findAllByOrderBySortOrderAsc();

    void deleteByArtworkId(Long artworkId);
}
