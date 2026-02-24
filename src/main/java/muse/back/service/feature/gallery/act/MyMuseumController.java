package muse.back.service.feature.gallery.act;

import auth.common.core.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse.back.service.database.pub.dto.MyMuseumArtworkCreateRequest;
import muse.back.service.database.pub.dto.MyMuseumArtworkResponse;
import muse.back.service.database.pub.dto.MyMuseumCreateRequest;
import muse.back.service.database.pub.dto.MyMuseumResponse;
import muse.back.service.database.pub.dto.MyMuseumUpdateRequest;
import muse.back.service.feature.gallery.biz.MuseumService;
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
@RequestMapping("/api/muse/v1/me/museums")
@RequiredArgsConstructor
public class MyMuseumController {

    private final MuseumService museumService;

    @GetMapping
    public ResponseDataDTO<List<MyMuseumResponse>> getMyMuseums(UserContext userContext) {
        Long userId = requireUserId(userContext);
        return ResponseDataDTO.of(museumService.getMyMuseums(userId), "내 뮤지엄 목록 조회 성공");
    }

    @PostMapping
    public ResponseDataDTO<MyMuseumResponse> createMyMuseum(
            @RequestBody MyMuseumCreateRequest request,
            UserContext userContext
    ) {
        Long userId = requireUserId(userContext);
        log.info("Create museum by user: userId={}", userId);
        return ResponseDataDTO.of(
                museumService.createMyMuseum(userId, request),
                "뮤지엄 생성 성공"
        );
    }

    @PutMapping("/{museumId}")
    public ResponseDataDTO<MyMuseumResponse> updateMyMuseum(
            @PathVariable Long museumId,
            @RequestBody MyMuseumUpdateRequest request,
            UserContext userContext
    ) {
        Long userId = requireUserId(userContext);
        log.info("Update museum by user: userId={}, museumId={}", userId, museumId);
        return ResponseDataDTO.of(
                museumService.updateMyMuseum(museumId, userId, request),
                "뮤지엄 수정 성공"
        );
    }

    @DeleteMapping("/{museumId}")
    public ResponseDataDTO<Void> deleteMyMuseum(
            @PathVariable Long museumId,
            UserContext userContext
    ) {
        Long userId = requireUserId(userContext);
        log.info("Delete museum by user: userId={}, museumId={}", userId, museumId);
        museumService.deleteMyMuseum(museumId, userId);
        return ResponseDataDTO.of(null, "뮤지엄 삭제 성공");
    }

    @GetMapping("/{museumId}/artworks")
    public ResponseDataDTO<List<MyMuseumArtworkResponse>> getMyMuseumArtworks(
            @PathVariable Long museumId,
            UserContext userContext
    ) {
        Long userId = requireUserId(userContext);
        return ResponseDataDTO.of(
                museumService.getMyMuseumArtworks(museumId, userId),
                "내 뮤지엄 작품 목록 조회 성공"
        );
    }

    @PostMapping("/{museumId}/artworks")
    public ResponseDataDTO<MyMuseumArtworkResponse> createMyMuseumArtwork(
            @PathVariable Long museumId,
            @RequestBody MyMuseumArtworkCreateRequest request,
            UserContext userContext
    ) {
        Long userId = requireUserId(userContext);
        log.info("Create museum artwork by user: userId={}, museumId={}", userId, museumId);
        return ResponseDataDTO.of(
                museumService.createMyMuseumArtwork(museumId, userId, request),
                "뮤지엄 작품 등록 성공"
        );
    }

    @DeleteMapping("/{museumId}/artworks/{museumArtworkId}")
    public ResponseDataDTO<Void> deleteMyMuseumArtwork(
            @PathVariable Long museumId,
            @PathVariable Long museumArtworkId,
            UserContext userContext
    ) {
        Long userId = requireUserId(userContext);
        log.info(
                "Delete museum artwork by user: userId={}, museumId={}, museumArtworkId={}",
                userId,
                museumId,
                museumArtworkId
        );
        museumService.deleteMyMuseumArtwork(museumId, museumArtworkId, userId);
        return ResponseDataDTO.of(null, "뮤지엄 작품 삭제 성공");
    }

    private Long requireUserId(UserContext userContext) {
        if (userContext == null || !userContext.isAuthenticated()) {
            throw new GeneralException(Code.UNAUTHORIZED, "Login required");
        }
        return userContext.getUserId();
    }
}
