package muse.back.service.feature.contest.act;

import auth.common.core.context.RequirePrincipalRole;
import auth.common.core.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse.back.service.feature.contest.biz.ContestService;
import muse.back.service.database.pub.dto.ContestDetailResponse;
import muse.back.service.database.pub.dto.ContestEntryCreditResponse;
import muse.back.service.database.pub.dto.ContestEntryRequest;
import muse.back.service.database.pub.dto.ContestPublicEntryPageResponse;
import muse.back.service.database.pub.dto.ContestPublicEntryResponse;
import muse.back.service.database.pub.dto.ContestRankingResponse;
import muse.back.service.database.pub.dto.ContestEntryResponse;
import muse.back.service.database.pub.dto.ContestSummaryResponse;
import muse.back.service.database.pub.dto.ContestVoteRequest;
import muse.back.service.database.pub.dto.ContestVoteResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.core.response.base.dto.ResponseDataDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/muse/v1/contests")
@RequiredArgsConstructor
public class ContestController {
    private final ContestService contestService;

    @GetMapping
    public ResponseDataDTO<List<ContestSummaryResponse>> getContests() {
        log.info("Get contest list");
        return ResponseDataDTO.of(contestService.getActiveContests(), "콘테스트 목록 조회 성공");
    }

    @GetMapping("/{id}")
    public ResponseDataDTO<ContestDetailResponse> getContestDetail(@PathVariable Long id) {
        log.info("Get contest detail: id={}", id);
        return ResponseDataDTO.of(contestService.getContestDetail(id), "콘테스트 상세 조회 성공");
    }

    @GetMapping("/{id}/entries")
    public ResponseDataDTO<List<ContestPublicEntryResponse>> getContestEntries(@PathVariable Long id) {
        log.info("Get contest entries: id={}", id);
        return ResponseDataDTO.of(contestService.getContestEntries(id), "콘테스트 출품 목록 조회 성공");
    }

    @GetMapping("/{id}/entries/page")
    public ResponseDataDTO<ContestPublicEntryPageResponse> getContestEntriesPage(
            @PathVariable Long id,
            @RequestParam(name = "mode", required = false) String mode,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size
    ) {
        log.info("Get contest entries page: id={}, mode={}, page={}, size={}", id, mode, page, size);
        return ResponseDataDTO.of(
                contestService.getContestEntriesPage(id, mode, page, size),
                "콘테스트 출품 페이지 조회 성공"
        );
    }

    @GetMapping("/{id}/ranking")
    public ResponseDataDTO<List<ContestRankingResponse>> getContestRanking(@PathVariable Long id) {
        log.info("Get contest ranking: id={}", id);
        return ResponseDataDTO.of(contestService.getContestRanking(id), "콘테스트 랭킹 조회 성공");
    }

    @PostMapping("/{id}/entry-credits/purchase")
    @RequirePrincipalRole
    public ResponseDataDTO<ContestEntryCreditResponse> purchaseEntryCredit(
            @PathVariable Long id,
            UserContext userContext
    ) {
        String userKey = requireUserKey(userContext);
        log.info("Purchase entry credit: id={}", id);
        return ResponseDataDTO.of(
                contestService.purchaseEntryCredit(id, userKey),
                "출품권 구매 성공"
        );
    }

    @PostMapping("/{id}/entries")
    @RequirePrincipalRole
    public ResponseDataDTO<ContestEntryResponse> submitEntry(
            @PathVariable Long id,
            @RequestBody ContestEntryRequest request,
            UserContext userContext
    ) {
        String userKey = requireUserKey(userContext);
        log.info("Submit contest entry: id={}, fileName={}", id, request.fileName());
        return ResponseDataDTO.of(
                contestService.submitEntry(
                        id,
                        userKey,
                        request.title(),
                        request.description(),
                        request.fileName(),
                        request.fileSizeBytes(),
                        request.imageWidthPx(),
                        request.imageHeightPx()
                ),
                "콘테스트 출품 등록 성공"
        );
    }

    @PostMapping("/{id}/votes")
    @RequirePrincipalRole
    public ResponseDataDTO<ContestVoteResponse> voteEntry(
            @PathVariable Long id,
            @RequestBody ContestVoteRequest request,
            UserContext userContext
    ) {
        String userKey = requireUserKey(userContext);
        log.info("Vote contest entry: contestId={}, entryId={}", id, request.entryId());
        return ResponseDataDTO.of(
                contestService.voteEntry(
                        id,
                        userKey,
                        request.entryId()
                ),
                "콘테스트 투표 성공"
        );
    }

    private String requireUserKey(UserContext userContext) {
        if (userContext == null || userContext.getUserKey() == null || userContext.getUserKey().isBlank()) {
            throw new GeneralException(Code.UNAUTHORIZED, "Login required");
        }
        return userContext.getUserKey();
    }
}
