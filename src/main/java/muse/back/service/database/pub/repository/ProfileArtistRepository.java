package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.ProfileArtist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProfileArtistRepository extends JpaRepository<ProfileArtist, Long> {

    Optional<ProfileArtist> findTopByOrderByArtistIdAsc();

    Optional<ProfileArtist> findByUserId(Long userId);

    List<ProfileArtist> findByArtistIdIn(Collection<Long> artistIds);
}
