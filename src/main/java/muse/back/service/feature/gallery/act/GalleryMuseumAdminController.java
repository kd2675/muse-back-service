package muse.back.service.feature.gallery.act;

import auth.common.core.constant.UserRole;
import auth.common.core.context.RequirePrincipalRole;
import auth.common.core.context.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse.back.service.database.pub.dto.AdminMuseumArtworkModerationUpdateRequest;
import muse.back.service.database.pub.dto.AdminMuseumArtworkResponse;
import muse.back.service.database.pub.dto.AdminMuseumFeatureUpdateRequest;
import muse.back.service.database.pub.dto.AdminMuseumResponse;
import muse.back.service.database.pub.dto.AdminMuseumVisibilityUpdateRequest;
import muse.back.service.feature.gallery.biz.MuseumService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.core.response.base.dto.ResponseDataDTO;

import java.util.List;

@Slf4j
@RestController
@RequirePrincipalRole(anyOf = {UserRole.ADMIN})
@RequestMapping("/api/muse/v1/admin/gallery/museums")
@RequiredArgsConstructor
public class GalleryMuseumAdminController {

    private final MuseumService museumService;

    @GetMapping
    public ResponseDataDTO<List<AdminMuseumResponse>> getAdminMuseums(UserContext userContext) {
        return ResponseDataDTO.of(museumService.getAdminMuseums(), "관리자 뮤지엄 목록 조회 성공");
    }

    @PutMapping("/{museumId}/featured")
    public ResponseDataDTO<AdminMuseumResponse> updateMuseumFeatured(
            @PathVariable Long museumId,
            @Valid @RequestBody AdminMuseumFeatureUpdateRequest request,
            UserContext userContext
    ) {
        log.info("Update museum featured by admin: museumId={}", museumId);
        return ResponseDataDTO.of(
                museumService.updateAdminMuseumFeatured(museumId, request),
                "뮤지엄 메인 노출 설정 변경 성공"
        );
    }

    @PutMapping("/{museumId}/visibility")
    public ResponseDataDTO<AdminMuseumResponse> updateMuseumVisibility(
            @PathVariable Long museumId,
            @Valid @RequestBody AdminMuseumVisibilityUpdateRequest request,
            UserContext userContext
    ) {
        log.info("Update museum visibility by admin: museumId={}", museumId);
        return ResponseDataDTO.of(
                museumService.updateAdminMuseumVisibility(museumId, request),
                "뮤지엄 공개 설정 변경 성공"
        );
    }

    @GetMapping("/{museumId}/artworks")
    public ResponseDataDTO<List<AdminMuseumArtworkResponse>> getAdminMuseumArtworks(
            @PathVariable Long museumId,
            UserContext userContext
    ) {
        return ResponseDataDTO.of(
                museumService.getAdminMuseumArtworks(museumId),
                "관리자 뮤지엄 작품 목록 조회 성공"
        );
    }

    @PutMapping("/{museumId}/artworks/{museumArtworkId}/moderation")
    public ResponseDataDTO<AdminMuseumArtworkResponse> updateMuseumArtworkModeration(
            @PathVariable Long museumId,
            @PathVariable Long museumArtworkId,
            @Valid @RequestBody AdminMuseumArtworkModerationUpdateRequest request,
            UserContext userContext
    ) {
        log.info("Update museum artwork moderation by admin: museumId={}, museumArtworkId={}", museumId, museumArtworkId);
        return ResponseDataDTO.of(
                museumService.updateAdminMuseumArtworkModeration(museumId, museumArtworkId, request),
                "뮤지엄 작품 모더레이션 상태 변경 성공"
        );
    }

    @DeleteMapping("/{museumId}/artworks/{museumArtworkId}")
    public ResponseDataDTO<Void> deleteMuseumArtworkByAdmin(
            @PathVariable Long museumId,
            @PathVariable Long museumArtworkId,
            UserContext userContext
    ) {
        log.info("Delete museum artwork by admin: museumId={}, museumArtworkId={}", museumId, museumArtworkId);
        museumService.deleteAdminMuseumArtwork(museumId, museumArtworkId);
        return ResponseDataDTO.of(null, "뮤지엄 작품 삭제 성공");
    }
}
