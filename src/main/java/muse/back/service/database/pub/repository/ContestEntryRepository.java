package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.ContestEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ContestEntryRepository extends JpaRepository<ContestEntry, String> {

    List<ContestEntry> findByArtistIdOrderByCreateDateDesc(Long artistId);
    Page<ContestEntry> findByArtistId(Long artistId, Pageable pageable);

    List<ContestEntry> findByContestIdOrderByCreateDateDesc(Long contestId);

    List<ContestEntry> findByContestIdAndStatusInOrderByCreateDateDesc(Long contestId, Collection<String> statuses);

    List<ContestEntry> findByContestIdAndStatusIn(Long contestId, Collection<String> statuses);

    Page<ContestEntry> findByContestIdAndStatusIn(Long contestId, Collection<String> statuses, Pageable pageable);

    @Query(
            value = """
                    SELECT * FROM contest_entry
                    WHERE contest_id = :contestId
                      AND status IN (:statuses)
                    ORDER BY RAND()
                    LIMIT :size
                    """,
            nativeQuery = true
    )
    List<ContestEntry> findRandomByContestIdAndStatusIn(
            @Param("contestId") Long contestId,
            @Param("statuses") Collection<String> statuses,
            @Param("size") int size
    );

    long countByContestId(Long contestId);
    long countByArtistId(Long artistId);

    long countByContestIdAndStatusIn(Long contestId, Collection<String> statuses);

    boolean existsByContestIdAndStatusIn(Long contestId, Collection<String> statuses);

    Optional<ContestEntry> findByEntryIdAndContestId(String entryId, Long contestId);

    Optional<ContestEntry> findByEntryIdAndArtistId(String entryId, Long artistId);
}
