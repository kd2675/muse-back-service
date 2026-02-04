package muse.back.service.database.pub.dto;

import java.util.List;

public record GalleryCategoryResponse(
        Category category,
        List<ArtworkCard> artworks
) {
    public record Category(
            String key,
            String title,
            String description,
            int itemCount
    ) {}

    public record ArtworkCard(
            Long id,
            String title,
            String artist,
            String colorFrom,
            String colorTo
    ) {}
}
