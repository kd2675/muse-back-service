package muse.back.service.feature.overview.act;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse.back.service.database.pub.dto.OverviewResponse;
import muse.back.service.feature.overview.biz.OverviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.core.response.base.dto.ResponseDataDTO;

@Slf4j
@RestController
@RequestMapping("/api/muse/v1/overview")
@RequiredArgsConstructor
public class OverviewController {
    private final OverviewService overviewService;

    @GetMapping
    public ResponseDataDTO<OverviewResponse> getOverview() {
        log.info("Get overview data");
        return ResponseDataDTO.of(overviewService.getOverview(), "오버뷰 데이터 조회 성공");
    }
}
