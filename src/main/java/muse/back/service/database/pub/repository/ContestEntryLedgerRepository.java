package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.ContestEntryLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContestEntryLedgerRepository extends JpaRepository<ContestEntryLedger, Long> {

    boolean existsByArtistIdAndContestIdAndReasonAndRefId(
            Long artistId,
            Long contestId,
            String reason,
            String refId
    );

    List<ContestEntryLedger> findByContestIdAndReason(Long contestId, String reason);
}
