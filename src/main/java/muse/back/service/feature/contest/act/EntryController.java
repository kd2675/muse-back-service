package muse.back.service.feature.contest.act;

import auth.common.core.context.RequirePrincipalRole;
import auth.common.core.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse.back.service.feature.contest.biz.ContestService;
import muse.back.service.database.pub.dto.ContestEntrySummaryPageResponse;
import muse.back.service.database.pub.dto.ContestEntrySummaryResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import web.common.core.response.base.dto.ResponseDataDTO;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

import java.util.List;

@Slf4j
@RestController
@RequirePrincipalRole
@RequestMapping("/api/muse/v1/me/entries")
@RequiredArgsConstructor
public class EntryController {

    private final ContestService contestService;

    @GetMapping
    public ResponseDataDTO<List<ContestEntrySummaryResponse>> getMyEntries(
            UserContext userContext
    ) {
        String userKey = requireUserKey(userContext);
        log.info("Get my contest entries");
        return ResponseDataDTO.of(contestService.getMyEntries(userKey), "출품 목록 조회 성공");
    }

    @GetMapping("/page")
    public ResponseDataDTO<ContestEntrySummaryPageResponse> getMyEntriesPage(
            UserContext userContext,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size
    ) {
        String userKey = requireUserKey(userContext);
        log.info("Get my contest entries page: page={}, size={}", page, size);
        return ResponseDataDTO.of(
                contestService.getMyEntriesPage(userKey, page, size),
                "출품 목록 페이지 조회 성공"
        );
    }

    @DeleteMapping("/{entryId}")
    public ResponseDataDTO<Void> deleteEntry(
            @PathVariable String entryId,
            UserContext userContext
    ) {
        String userKey = requireUserKey(userContext);
        log.info("Delete contest entry: entryId={}", entryId);
        contestService.deleteEntry(entryId, userKey);
        return ResponseDataDTO.of(null, "출품 삭제 성공");
    }

    private String requireUserKey(UserContext userContext) {
        if (userContext == null || userContext.getUserKey() == null || userContext.getUserKey().isBlank()) {
            throw new GeneralException(Code.UNAUTHORIZED, "Login required");
        }
        return userContext.getUserKey();
    }
}
