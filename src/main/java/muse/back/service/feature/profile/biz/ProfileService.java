package muse.back.service.feature.profile.biz;

import muse.back.service.database.pub.dto.ProfileSummaryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {

    public ProfileSummaryResponse getProfileSummary() {
        ProfileSummaryResponse.Artist artist = new ProfileSummaryResponse.Artist(
                501L,
                "Minji Han",
                "빛과 질감을 탐구하는 사진가",
                "#2B2A28"
        );

        ProfileSummaryResponse.Stats stats = new ProfileSummaryResponse.Stats(
                42,
                5,
                1530000,
                1280
        );

        List<ProfileSummaryResponse.PortfolioItem> portfolio = List.of(
                new ProfileSummaryResponse.PortfolioItem(
                        901L,
                        "Silk City",
                        "Urban",
                        "#1E2A35",
                        "#6B7C93"
                ),
                new ProfileSummaryResponse.PortfolioItem(
                        902L,
                        "Midnight Bloom",
                        "Night",
                        "#1B1D2E",
                        "#5A7AA6"
                ),
                new ProfileSummaryResponse.PortfolioItem(
                        903L,
                        "Quiet Spring",
                        "Nature",
                        "#4C5B3C",
                        "#C6D19C"
                )
        );

        List<ProfileSummaryResponse.AwardItem> awards = List.of(
                new ProfileSummaryResponse.AwardItem(
                        701L,
                        "빛의 레이어",
                        "1st",
                        "500,000원",
                        "2026.01"
                ),
                new ProfileSummaryResponse.AwardItem(
                        702L,
                        "도시의 숨",
                        "2nd",
                        "300,000원",
                        "2025.12"
                )
        );

        return new ProfileSummaryResponse(artist, stats, portfolio, awards);
    }
}
