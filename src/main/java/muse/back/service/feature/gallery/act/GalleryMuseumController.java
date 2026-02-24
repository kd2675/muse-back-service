package muse.back.service.feature.gallery.act;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse.back.service.database.pub.dto.PublicMuseumDetailResponse;
import muse.back.service.database.pub.dto.PublicMuseumSummaryResponse;
import muse.back.service.feature.gallery.biz.MuseumService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.core.response.base.dto.ResponseDataDTO;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/muse/v1/gallery/museums")
@RequiredArgsConstructor
public class GalleryMuseumController {

    private final MuseumService museumService;

    @GetMapping
    public ResponseDataDTO<List<PublicMuseumSummaryResponse>> getPublicMuseums() {
        log.info("Get public museums");
        return ResponseDataDTO.of(museumService.getPublicMuseums(), "뮤지엄 목록 조회 성공");
    }

    @GetMapping("/{museumId}")
    public ResponseDataDTO<PublicMuseumDetailResponse> getPublicMuseumDetail(@PathVariable Long museumId) {
        log.info("Get public museum detail: museumId={}", museumId);
        return ResponseDataDTO.of(museumService.getPublicMuseumDetail(museumId), "뮤지엄 상세 조회 성공");
    }
}
