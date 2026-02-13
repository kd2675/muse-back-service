package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.Artwork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtworkRepository extends JpaRepository<Artwork, Long> {

    List<Artwork> findByCategoryKeyOrderByArtworkIdAsc(String categoryKey);
}
