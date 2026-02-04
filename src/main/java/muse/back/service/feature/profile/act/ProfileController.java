package muse.back.service.feature.profile.act;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse.back.service.database.pub.dto.ProfileSummaryResponse;
import muse.back.service.feature.profile.biz.ProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.core.response.base.dto.ResponseDataDTO;

@Slf4j
@RestController
@RequestMapping("/api/muse/v1/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping("/summary")
    public ResponseDataDTO<ProfileSummaryResponse> getProfileSummary() {
        log.info("Get profile summary");
        return ResponseDataDTO.of(profileService.getProfileSummary(), "프로필 요약 조회 성공");
    }
}
