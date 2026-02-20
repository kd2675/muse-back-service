package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.ArtworkAsset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtworkAssetRepository extends JpaRepository<ArtworkAsset, Long> {

    void deleteByArtworkId(Long artworkId);
}
