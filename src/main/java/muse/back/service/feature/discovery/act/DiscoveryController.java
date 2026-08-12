package muse.back.service.feature.discovery.act;

import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.DiscoverySearchResponse;
import muse.back.service.feature.discovery.biz.DiscoveryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import web.common.core.response.base.dto.ResponseDataDTO;

@RestController
@RequestMapping("/api/muse/v1/discovery")
@RequiredArgsConstructor
public class DiscoveryController {
    private final DiscoveryService discoveryService;

    @GetMapping("/search")
    public ResponseDataDTO<DiscoverySearchResponse> search(@RequestParam("q") String query) {
        return ResponseDataDTO.of(discoveryService.search(query), "통합 탐색 성공");
    }
}
