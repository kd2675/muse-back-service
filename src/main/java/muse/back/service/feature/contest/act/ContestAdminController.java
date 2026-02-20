package muse.back.service.feature.contest.act;

import auth.common.core.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse.back.service.database.pub.dto.AdminContestResponse;
import muse.back.service.database.pub.dto.AdminContestUpsertRequest;
import muse.back.service.database.pub.dto.ContestFinalizeResponse;
import muse.back.service.feature.contest.biz.ContestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.core.response.base.dto.ResponseDataDTO;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/muse/v1/admin/contests")
@RequiredArgsConstructor
public class ContestAdminController {

    private final ContestService contestService;

    @GetMapping
    public ResponseDataDTO<List<AdminContestResponse>> getAdminContests(UserContext userContext) {
        requireAdmin(userContext);
        return ResponseDataDTO.of(contestService.getAdminContests(), "관리자 콘테스트 목록 조회 성공");
    }

    @PostMapping
    public ResponseDataDTO<AdminContestResponse> createContest(
            @RequestBody AdminContestUpsertRequest request,
            UserContext userContext
    ) {
        requireAdmin(userContext);
        log.info("Create contest by admin");
        return ResponseDataDTO.of(contestService.createContest(request), "콘테스트 생성 성공");
    }

    @PutMapping("/{id}")
    public ResponseDataDTO<AdminContestResponse> updateContest(
            @PathVariable Long id,
            @RequestBody AdminContestUpsertRequest request,
            UserContext userContext
    ) {
        requireAdmin(userContext);
        log.info("Update contest by admin: id={}", id);
        return ResponseDataDTO.of(contestService.updateContest(id, request), "콘테스트 수정 성공");
    }

    @PostMapping("/{id}/finalize")
    public ResponseDataDTO<ContestFinalizeResponse> finalizeContest(
            @PathVariable Long id,
            UserContext userContext
    ) {
        requireAdmin(userContext);
        log.info("Finalize contest by admin: id={}", id);
        return ResponseDataDTO.of(contestService.finalizeContestResults(id), "콘테스트 결과 확정 성공");
    }

    private void requireAdmin(UserContext userContext) {
        if (userContext == null || !userContext.isAuthenticated()) {
            throw new GeneralException(Code.UNAUTHORIZED, "Login required");
        }
        if (!userContext.isAdmin()) {
            throw new GeneralException(Code.FORBIDDEN, "Admin role required");
        }
    }
}
