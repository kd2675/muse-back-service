package muse.back.service.feature.profile.act;

import auth.common.core.context.RequirePrincipalRole;
import auth.common.core.context.UserContext;
import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.FollowStatusResponse;
import muse.back.service.database.pub.dto.PublicArtistResponse;
import muse.back.service.feature.profile.biz.PublicArtistService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.core.response.base.dto.ResponseDataDTO;

@RestController
@RequestMapping("/api/muse/v1/artists")
@RequiredArgsConstructor
public class PublicArtistController {
    private final PublicArtistService publicArtistService;

    @GetMapping("/{artistId}")
    public ResponseDataDTO<PublicArtistResponse> getArtist(@PathVariable Long artistId) {
        return ResponseDataDTO.of(publicArtistService.getArtist(artistId), "작가 공개 기록 조회 성공");
    }

    @GetMapping("/{artistId}/follow-status")
    @RequirePrincipalRole
    public ResponseDataDTO<FollowStatusResponse> getFollowStatus(@PathVariable Long artistId, UserContext context) {
        return ResponseDataDTO.of(publicArtistService.getFollowStatus(context.getUserKey(), artistId), "팔로우 상태 조회 성공");
    }

    @PostMapping("/{artistId}/followers")
    @RequirePrincipalRole
    public ResponseDataDTO<FollowStatusResponse> follow(@PathVariable Long artistId, UserContext context) {
        return ResponseDataDTO.of(publicArtistService.follow(context.getUserKey(), artistId), "작가 팔로우 성공");
    }

    @DeleteMapping("/{artistId}/followers")
    @RequirePrincipalRole
    public ResponseDataDTO<FollowStatusResponse> unfollow(@PathVariable Long artistId, UserContext context) {
        return ResponseDataDTO.of(publicArtistService.unfollow(context.getUserKey(), artistId), "작가 팔로우 해제 성공");
    }
}
