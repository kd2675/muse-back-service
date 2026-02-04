package muse.back.service.database.pub.dto;

import java.util.List;

public record GalleryLobbyResponse(
        List<Hero> highlights,
        List<CategoryCard> categories
) {
    public record Hero(
            Long id,
            String title,
            String artist,
            String category,
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
}
