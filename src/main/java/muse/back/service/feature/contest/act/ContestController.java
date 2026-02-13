package muse.back.service.feature.contest.act;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import auth.common.core.context.UserContext;
import muse.back.service.feature.contest.biz.ContestService;
import muse.back.service.database.pub.dto.ContestDetailResponse;
import muse.back.service.database.pub.dto.ContestEntryCreditResponse;
import muse.back.service.database.pub.dto.ContestEntryRequest;
import muse.back.service.database.pub.dto.ContestEntryResponse;
import muse.back.service.database.pub.dto.ContestSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.core.response.base.dto.ResponseDataDTO;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/{id}/entry-credits/purchase")
    public ResponseDataDTO<ContestEntryCreditResponse> purchaseEntryCredit(
            @PathVariable Long id,
            UserContext userContext
    ) {
        Long userId = requireUserId(userContext);
        log.info("Purchase entry credit: id={}", id);
        return ResponseDataDTO.of(
                contestService.purchaseEntryCredit(id, userId),
                "출품권 구매 성공"
        );
    }

    @PostMapping("/{id}/entries")
    public ResponseDataDTO<ContestEntryResponse> submitEntry(
            @PathVariable Long id,
            @RequestBody ContestEntryRequest request,
            UserContext userContext
    ) {
        Long userId = requireUserId(userContext);
        log.info("Submit contest entry: id={}, imageUrl={}", id, request.imageUrl());
        return ResponseDataDTO.of(
                contestService.submitEntry(
                        id,
                        userId,
                        request.title(),
                        request.description(),
                        request.fileName(),
                        request.imageUrl()
                ),
                "콘테스트 출품 등록 성공"
        );
    }

    private Long requireUserId(UserContext userContext) {
        if (userContext == null || !userContext.isAuthenticated()) {
            throw new GeneralException(Code.UNAUTHORIZED, "Login required");
        }
        return userContext.getUserId();
    }
}
