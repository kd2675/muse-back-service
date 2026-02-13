package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.ContestEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContestEntryRepository extends JpaRepository<ContestEntry, String> {

    List<ContestEntry> findByArtistIdOrderByCreateDateDesc(Long artistId);

    Optional<ContestEntry> findByEntryIdAndArtistId(String entryId, Long artistId);
}
