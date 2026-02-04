package muse.back.service.feature.home.act;

import muse.back.service.database.pub.dto.HomeResponse;
import muse.back.service.feature.home.biz.HomeService;
import web.common.core.response.base.dto.ResponseDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/muse/v1/home")
@RequiredArgsConstructor
public class HomeController {
    private final HomeService homeService;

    @GetMapping
    public ResponseDataDTO<HomeResponse> getHome() {
        log.info("Get home data");
        return ResponseDataDTO.of(homeService.getHome(), "홈 데이터 조회 성공");
    }
}
