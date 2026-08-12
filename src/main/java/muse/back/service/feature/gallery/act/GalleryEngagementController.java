package muse.back.service.feature.gallery.act;

import auth.common.core.context.RequirePrincipalRole;
import auth.common.core.context.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.MuseumBookmarkResponse;
import muse.back.service.database.pub.dto.MuseumViewRequest;
import muse.back.service.database.pub.dto.MuseumViewResponse;
import muse.back.service.feature.gallery.biz.GalleryEngagementService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.core.response.base.dto.ResponseDataDTO;

import java.util.List;

@RestController
@RequirePrincipalRole
@RequestMapping("/api/muse/v1/me/gallery")
@RequiredArgsConstructor
public class GalleryEngagementController {
    private final GalleryEngagementService engagementService;

    @GetMapping("/bookmarks")
    public ResponseDataDTO<List<MuseumBookmarkResponse>> bookmarks(UserContext context) {
        return ResponseDataDTO.of(engagementService.getBookmarks(context.getUserKey()), "저장한 전시 조회 성공");
    }

    @GetMapping("/museums/{museumId}/bookmark")
    public ResponseDataDTO<MuseumBookmarkResponse> bookmarkStatus(@PathVariable Long museumId, UserContext context) {
        return ResponseDataDTO.of(engagementService.getBookmarkStatus(context.getUserKey(), museumId), "전시 저장 상태 조회 성공");
    }

    @PostMapping("/museums/{museumId}/bookmark")
    public ResponseDataDTO<MuseumBookmarkResponse> bookmark(@PathVariable Long museumId, UserContext context) {
        return ResponseDataDTO.of(engagementService.bookmark(context.getUserKey(), museumId), "전시 저장 성공");
    }

    @DeleteMapping("/museums/{museumId}/bookmark")
    public ResponseDataDTO<MuseumBookmarkResponse> removeBookmark(@PathVariable Long museumId, UserContext context) {
        return ResponseDataDTO.of(engagementService.removeBookmark(context.getUserKey(), museumId), "전시 저장 해제 성공");
    }

    @GetMapping("/history")
    public ResponseDataDTO<List<MuseumViewResponse>> history(UserContext context) {
        return ResponseDataDTO.of(engagementService.getHistory(context.getUserKey()), "감상 기록 조회 성공");
    }

    @PutMapping("/museums/{museumId}/history")
    public ResponseDataDTO<MuseumViewResponse> recordView(
            @PathVariable Long museumId,
            @Valid @RequestBody MuseumViewRequest request,
            UserContext context
    ) {
        return ResponseDataDTO.of(engagementService.recordView(context.getUserKey(), museumId, request), "감상 기록 저장 성공");
    }
}
