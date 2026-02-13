package muse.back.service.feature.profile.act;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import auth.common.core.context.UserContext;
import muse.back.service.database.pub.dto.ProfileSummaryResponse;
import muse.back.service.feature.profile.biz.ProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.core.response.base.dto.ResponseDataDTO;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

@Slf4j
@RestController
@RequestMapping("/api/muse/v1/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping("/summary")
    public ResponseDataDTO<ProfileSummaryResponse> getProfileSummary(UserContext userContext) {
        Long userId = requireUserId(userContext);
        log.info("Get profile summary");
        return ResponseDataDTO.of(profileService.getProfileSummary(userId), "프로필 요약 조회 성공");
    }

    @PostMapping("/initialize")
    public ResponseDataDTO<ProfileSummaryResponse> initializeProfile(UserContext userContext) {
        Long userId = requireUserId(userContext);
        String userName = userContext.getUserName();
        log.info("Initialize profile: userId={}", userId);
        return ResponseDataDTO.of(
                profileService.initializeProfile(userId, userName),
                "프로필 생성 성공"
        );
    }

    private Long requireUserId(UserContext userContext) {
        if (userContext == null || !userContext.isAuthenticated()) {
            throw new GeneralException(Code.UNAUTHORIZED, "Login required");
        }
        return userContext.getUserId();
    }
}
