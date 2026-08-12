package muse.back.service.feature.contest.act;

import auth.common.core.context.RequirePrincipalRole;
import auth.common.core.context.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.ContestEntryDraftRequest;
import muse.back.service.database.pub.dto.ContestEntryDraftResponse;
import muse.back.service.feature.contest.biz.ContestDraftService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.core.response.base.dto.ResponseDataDTO;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

@RestController
@RequirePrincipalRole
@RequestMapping("/api/muse/v1/me/contests/{contestId}/draft")
@RequiredArgsConstructor
public class ContestDraftController {
    private final ContestDraftService draftService;

    @GetMapping
    public ResponseDataDTO<ContestEntryDraftResponse> get(
            @PathVariable Long contestId,
            UserContext userContext
    ) {
        return ResponseDataDTO.of(draftService.get(requireUserKey(userContext), contestId), "출품 초안 조회 성공");
    }

    @PutMapping
    public ResponseDataDTO<ContestEntryDraftResponse> save(
            @PathVariable Long contestId,
            @Valid @RequestBody ContestEntryDraftRequest request,
            UserContext userContext
    ) {
        return ResponseDataDTO.of(
                draftService.save(requireUserKey(userContext), contestId, request),
                "출품 초안 저장 성공"
        );
    }

    @DeleteMapping
    public ResponseDataDTO<Void> delete(@PathVariable Long contestId, UserContext userContext) {
        draftService.delete(requireUserKey(userContext), contestId);
        return ResponseDataDTO.of(null, "출품 초안 삭제 성공");
    }

    private String requireUserKey(UserContext context) {
        if (context == null || context.getUserKey() == null || context.getUserKey().isBlank()) {
            throw new GeneralException(Code.UNAUTHORIZED, "Login required");
        }
        return context.getUserKey();
    }
}
