package muse.back.service.feature.artwork.act;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse.back.service.feature.artwork.biz.ArtworkService;
import muse.back.service.database.pub.dto.ArtworkDetailResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.core.response.base.dto.ResponseDataDTO;

@Slf4j
@RestController
@RequestMapping("/api/muse/v1/artworks")
@RequiredArgsConstructor
public class ArtworkController {
    private final ArtworkService artworkService;

    @GetMapping("/{id}")
    public ResponseDataDTO<ArtworkDetailResponse> getArtworkDetail(
            @PathVariable Long id
    ) {
        log.info("Get artwork detail: id={}", id);
        return ResponseDataDTO.of(artworkService.getArtworkDetail(id), "작품 상세 조회 성공");
    }
}
