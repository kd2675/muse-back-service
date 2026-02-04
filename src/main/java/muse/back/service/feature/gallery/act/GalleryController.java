package muse.back.service.feature.gallery.act;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse.back.service.database.pub.dto.GalleryLobbyResponse;
import muse.back.service.database.pub.dto.GalleryCategoryResponse;
import muse.back.service.feature.gallery.biz.GalleryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.core.response.base.dto.ResponseDataDTO;

@Slf4j
@RestController
@RequestMapping("/api/muse/v1/gallery")
@RequiredArgsConstructor
public class GalleryController {
    private final GalleryService galleryService;

    @GetMapping("/lobby")
    public ResponseDataDTO<GalleryLobbyResponse> getLobby() {
        log.info("Get gallery lobby");
        return ResponseDataDTO.of(galleryService.getLobby(), "갤러리 로비 조회 성공");
    }

    @GetMapping("/categories/{key}")
    public ResponseDataDTO<GalleryCategoryResponse> getCategory(
            @PathVariable String key
    ) {
        log.info("Get gallery category: key={}", key);
        return ResponseDataDTO.of(galleryService.getCategoryDetail(key), "갤러리 카테고리 조회 성공");
    }
}
