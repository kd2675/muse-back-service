package muse.back.service.database.pub.dto;

import java.util.List;

public record HomeResponse(
        Hero hero,
        List<ArtworkCard> todaysPick,
        List<CategoryCard> galleryCategories,
        List<ContestCard> activeContests
) {
    public record Hero(
            String badge,
            String headline,
            String subheadline,
            String description
    ) {}

    public record ArtworkCard(
            Long id,
            String title,
            String artist,
            String category,
            String camera,
            String colorFrom,
            String colorTo
    ) {}

    public record CategoryCard(
            String key,
            String title,
            String description,
            int itemCount,
            String colorFrom,
            String colorTo
    ) {}

    public record ContestCard(
            Long id,
            String theme,
            String period,
            int entryFee,
            int prizePool,
            int daysLeft
    ) {}
}
