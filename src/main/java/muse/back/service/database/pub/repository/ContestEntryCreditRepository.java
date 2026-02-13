package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.ContestEntryCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface ContestEntryCreditRepository extends JpaRepository<ContestEntryCredit, Long> {

    Optional<ContestEntryCredit> findByArtistIdAndContestId(Long artistId, Long contestId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select credit
            from ContestEntryCredit credit
            where credit.artistId = :artistId
              and credit.contestId = :contestId
            """)
    Optional<ContestEntryCredit> findByArtistIdAndContestIdForUpdate(
            @Param("artistId") Long artistId,
            @Param("contestId") Long contestId
    );
}
