package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.Contest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContestRepository extends JpaRepository<Contest, Long> {

    List<Contest> findAllByOrderByContestIdAsc();

    List<Contest> findTop20ByThemeContainingIgnoreCaseOrderByContestIdDesc(String theme);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select contest from Contest contest where contest.contestId = :contestId")
    Optional<Contest> findByIdForUpdate(@Param("contestId") Long contestId);
}
