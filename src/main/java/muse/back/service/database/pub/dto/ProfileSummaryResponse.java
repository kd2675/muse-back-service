package muse.back.service.database.pub.dto;

import java.util.List;

public record ProfileSummaryResponse(
        Artist artist,
        Stats stats,
        List<PortfolioItem> portfolio,
        List<AwardItem> awards
) {
    public record Artist(
            Long id,
            String name,
            String tagline,
            String profileColor
    ) {}

    public record Stats(
            int totalWorks,
            int totalAwards,
            int totalEarnings,
            int followers
    ) {}

    public record PortfolioItem(
            Long id,
            String title,
            String category,
            String colorFrom,
            String colorTo
    ) {}

    public record AwardItem(
            Long id,
            String contest,
            String rank,
            String prize,
            String period
    ) {}
}
