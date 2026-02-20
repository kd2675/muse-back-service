package muse.back.service.feature.gallery.act;

import auth.common.core.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse.back.service.database.pub.dto.AdminGalleryCategoryResponse;
import muse.back.service.database.pub.dto.AdminGalleryCategoryCreateRequest;
import muse.back.service.database.pub.dto.AdminGalleryCategoryUpdateRequest;
import muse.back.service.database.pub.dto.AdminGalleryHighlightResponse;
import muse.back.service.database.pub.dto.AdminGalleryHighlightUpdateRequest;
import muse.back.service.database.pub.dto.AdminGalleryArtworkCreateRequest;
import muse.back.service.database.pub.dto.AdminGalleryArtworkResponse;
import muse.back.service.feature.gallery.biz.GalleryService;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/muse/v1/admin/gallery")
@RequiredArgsConstructor
public class GalleryAdminController {

    private final GalleryService galleryService;

    @GetMapping("/categories")
    public ResponseDataDTO<List<AdminGalleryCategoryResponse>> getAdminCategories(
            UserContext userContext
    ) {
        requireAdmin(userContext);
        return ResponseDataDTO.of(galleryService.getAdminCategories(), "갤러리 카테고리 목록 조회 성공");
    }

    @PutMapping("/categories/{key}")
    public ResponseDataDTO<AdminGalleryCategoryResponse> updateAdminCategory(
            @PathVariable String key,
            @RequestBody AdminGalleryCategoryUpdateRequest request,
            UserContext userContext
    ) {
        requireAdmin(userContext);
        log.info("Update gallery category by admin: key={}", key);
        return ResponseDataDTO.of(
                galleryService.updateAdminCategory(key, request),
                "갤러리 카테고리 수정 성공"
        );
    }

    @PostMapping("/categories")
    public ResponseDataDTO<AdminGalleryCategoryResponse> createAdminCategory(
            @RequestBody AdminGalleryCategoryCreateRequest request,
            UserContext userContext
    ) {
        requireAdmin(userContext);
        log.info("Create gallery category by admin: key={}", request == null ? null : request.key());
        return ResponseDataDTO.of(
                galleryService.createAdminCategory(request),
                "갤러리 카테고리 생성 성공"
        );
    }

    @DeleteMapping("/categories/{key}")
    public ResponseDataDTO<Void> deleteAdminCategory(
            @PathVariable String key,
            UserContext userContext
    ) {
        requireAdmin(userContext);
        log.info("Delete gallery category by admin: key={}", key);
        galleryService.deleteAdminCategory(key);
        return ResponseDataDTO.of(null, "갤러리 카테고리 삭제 성공");
    }

    @GetMapping("/highlights")
    public ResponseDataDTO<List<AdminGalleryHighlightResponse>> getAdminHighlights(
            UserContext userContext
    ) {
        requireAdmin(userContext);
        return ResponseDataDTO.of(galleryService.getAdminHighlights(), "갤러리 하이라이트 목록 조회 성공");
    }

    @GetMapping("/artworks")
    public ResponseDataDTO<List<AdminGalleryArtworkResponse>> getAdminArtworks(
            UserContext userContext
    ) {
        requireAdmin(userContext);
        return ResponseDataDTO.of(galleryService.getAdminArtworks(), "갤러리 작품 목록 조회 성공");
    }

    @PutMapping("/highlights")
    public ResponseDataDTO<List<AdminGalleryHighlightResponse>> replaceAdminHighlights(
            @RequestBody AdminGalleryHighlightUpdateRequest request,
            UserContext userContext
    ) {
        requireAdmin(userContext);
        log.info("Replace gallery highlights by admin");
        return ResponseDataDTO.of(
                galleryService.replaceAdminHighlights(request),
                "갤러리 하이라이트 수정 성공"
        );
    }

    @PostMapping("/artworks")
    public ResponseDataDTO<AdminGalleryArtworkResponse> createAdminArtwork(
            @RequestBody AdminGalleryArtworkCreateRequest request,
            UserContext userContext
    ) {
        requireAdmin(userContext);
        log.info("Create gallery artwork by admin: category={}", request == null ? null : request.categoryKey());
        return ResponseDataDTO.of(
                galleryService.createAdminArtwork(request),
                "갤러리 작품 추가 성공"
        );
    }

    @DeleteMapping("/artworks/{artworkId}")
    public ResponseDataDTO<Void> deleteAdminArtwork(
            @PathVariable Long artworkId,
            UserContext userContext
    ) {
        requireAdmin(userContext);
        log.info("Delete gallery artwork by admin: artworkId={}", artworkId);
        galleryService.deleteAdminArtwork(artworkId);
        return ResponseDataDTO.of(null, "갤러리 작품 삭제 성공");
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
