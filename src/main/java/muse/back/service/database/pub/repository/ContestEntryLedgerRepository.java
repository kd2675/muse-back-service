package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.ContestEntryLedger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestEntryLedgerRepository extends JpaRepository<ContestEntryLedger, Long> {
}
