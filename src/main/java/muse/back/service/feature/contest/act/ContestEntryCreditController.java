package muse.back.service.feature.contest.act;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse.back.service.database.pub.dto.ContestEntryCreditResponse;
import muse.back.service.feature.contest.biz.ContestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.core.response.base.dto.ResponseDataDTO;

@Slf4j
@RestController
@RequestMapping("/api/muse/v1/me/contests")
@RequiredArgsConstructor
public class ContestEntryCreditController {

    private final ContestService contestService;

    @GetMapping("/{id}/entry-credits")
    public ResponseDataDTO<ContestEntryCreditResponse> getEntryCredits(
            @PathVariable Long id
    ) {
        log.info("Get contest entry credits: id={}", id);
        return ResponseDataDTO.of(
                contestService.getEntryCreditStatus(id),
                "출품권 조회 성공"
        );
    }
}
