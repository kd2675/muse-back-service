package muse.back.service.database.pub.repository;

import muse.back.service.database.pub.entity.ContestEntryDraft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContestEntryDraftRepository extends JpaRepository<ContestEntryDraft, Long> {
    Optional<ContestEntryDraft> findByArtistIdAndContestId(Long artistId, Long contestId);
    void deleteByArtistIdAndContestId(Long artistId, Long contestId);
}
