package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.ProfileArtist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileArtistRepository extends JpaRepository<ProfileArtist, Long> {

    Optional<ProfileArtist> findTopByOrderByArtistIdAsc();

    Optional<ProfileArtist> findByUserId(Long userId);
}
