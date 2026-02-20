package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.ContestEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ContestEntryRepository extends JpaRepository<ContestEntry, String> {

    List<ContestEntry> findByArtistIdOrderByCreateDateDesc(Long artistId);

    List<ContestEntry> findByContestIdAndStatusInOrderByCreateDateDesc(Long contestId, Collection<String> statuses);

    List<ContestEntry> findByContestIdAndStatusIn(Long contestId, Collection<String> statuses);

    boolean existsByContestIdAndStatusIn(Long contestId, Collection<String> statuses);

    Optional<ContestEntry> findByEntryIdAndContestId(String entryId, Long contestId);

    Optional<ContestEntry> findByEntryIdAndArtistId(String entryId, Long artistId);
}
