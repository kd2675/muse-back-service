package muse.back.service.feature.contest.act;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse.back.service.feature.contest.biz.ContestService;
import muse.back.service.database.pub.dto.ContestEntrySummaryResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.core.response.base.dto.ResponseDataDTO;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/muse/v1/me/entries")
@RequiredArgsConstructor
public class EntryController {

    private final ContestService contestService;

    @GetMapping
    public ResponseDataDTO<List<ContestEntrySummaryResponse>> getMyEntries() {
        log.info("Get my contest entries");
        return ResponseDataDTO.of(contestService.getMyEntries(), "출품 목록 조회 성공");
    }

    @DeleteMapping("/{entryId}")
    public ResponseDataDTO<Void> deleteEntry(@PathVariable String entryId) {
        log.info("Delete contest entry: entryId={}", entryId);
        contestService.deleteEntry(entryId);
        return ResponseDataDTO.of(null, "출품 삭제 성공");
    }
}
