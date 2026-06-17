package muse.back.service.feature.contest.act;

import auth.common.core.context.RequirePrincipalRole;
import auth.common.core.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse.back.service.database.pub.dto.ContestEntryCreditResponse;
import muse.back.service.feature.contest.biz.ContestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.core.response.base.dto.ResponseDataDTO;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

@Slf4j
@RestController
@RequirePrincipalRole
@RequestMapping("/api/muse/v1/me/contests")
@RequiredArgsConstructor
public class ContestEntryCreditController {

    private final ContestService contestService;

    @GetMapping("/{id}/entry-credits")
    public ResponseDataDTO<ContestEntryCreditResponse> getEntryCredits(
            @PathVariable Long id,
            UserContext userContext
    ) {
        String userKey = requireUserKey(userContext);
        log.info("Get contest entry credits: id={}", id);
        return ResponseDataDTO.of(
                contestService.getEntryCreditStatus(id, userKey),
                "출품권 조회 성공"
        );
    }

    private String requireUserKey(UserContext userContext) {
        if (userContext == null || userContext.getUserKey() == null || userContext.getUserKey().isBlank()) {
            throw new GeneralException(Code.UNAUTHORIZED, "Login required");
        }
        return userContext.getUserKey();
    }
}
