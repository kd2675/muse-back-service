package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.ArtistFollow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ArtistFollowRepository extends JpaRepository<ArtistFollow, Long> {
    Optional<ArtistFollow> findByFollowerArtistIdAndFollowedArtistId(Long followerArtistId, Long followedArtistId);
    boolean existsByFollowerArtistIdAndFollowedArtistId(Long followerArtistId, Long followedArtistId);
    long countByFollowedArtistId(Long followedArtistId);
    List<ArtistFollow> findByFollowedArtistId(Long followedArtistId);
}
