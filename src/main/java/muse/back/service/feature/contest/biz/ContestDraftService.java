package muse.back.service.feature.contest.biz;

import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.ContestEntryDraftRequest;
import muse.back.service.database.pub.dto.ContestEntryDraftResponse;
import muse.back.service.database.pub.entity.ContestEntryDraft;
import muse.back.service.database.pub.repository.ContestEntryDraftRepository;
import muse.back.service.database.pub.repository.ContestRepository;
import muse.back.service.feature.profile.biz.ArtistIdentityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestDraftService {
    private final ContestEntryDraftRepository draftRepository;
    private final ContestRepository contestRepository;
    private final ArtistIdentityService artistIdentityService;

    public ContestEntryDraftResponse get(String userKey, Long contestId) {
        Long artistId = artistIdentityService.requireByUserKey(userKey).getArtistId();
        ContestEntryDraft draft = draftRepository.findByArtistIdAndContestId(artistId, contestId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Contest entry draft not found"));
        return toResponse(draft);
    }

    @Transactional
    public ContestEntryDraftResponse save(String userKey, Long contestId, ContestEntryDraftRequest request) {
        if (request == null) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Request body is required");
        }
        contestRepository.findById(contestId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Contest not found"));
        Long artistId = artistIdentityService.requireByUserKey(userKey).getArtistId();
        ContestEntryDraft draft = draftRepository.findByArtistIdAndContestId(artistId, contestId)
                .orElseGet(() -> new ContestEntryDraft(artistId, contestId, null, null));
        draft.update(trimToNull(request.title()), trimToNull(request.description()));
        return toResponse(draftRepository.saveAndFlush(draft));
    }

    @Transactional
    public void delete(String userKey, Long contestId) {
        Long artistId = artistIdentityService.requireByUserKey(userKey).getArtistId();
        draftRepository.deleteByArtistIdAndContestId(artistId, contestId);
    }

    private ContestEntryDraftResponse toResponse(ContestEntryDraft draft) {
        return new ContestEntryDraftResponse(
                draft.getContestId(), draft.getTitle(), draft.getDescription(), draft.getUpdatedAt()
        );
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
