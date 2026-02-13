package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.ProfileStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileStatRepository extends JpaRepository<ProfileStat, Long> {

    Optional<ProfileStat> findByArtistId(Long artistId);
}
