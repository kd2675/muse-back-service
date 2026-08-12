package muse.back.service.database.pub.repository;

import jakarta.persistence.LockModeType;
import muse.back.service.database.pub.entity.ProfileStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProfileStatRepository extends JpaRepository<ProfileStat, Long> {

    Optional<ProfileStat> findByArtistId(Long artistId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select stat from ProfileStat stat where stat.artistId = :artistId")
    Optional<ProfileStat> findByArtistIdForUpdate(@Param("artistId") Long artistId);
}
