package muse.back.service.database.pub.dto;

import java.util.List;

public record PublicMuseumDetailResponse(
        Long museumId,
        String name,
        String description,
        String ownerName,
        boolean isFeatured,
        List<Artwork> artworks
) {
    public record Artwork(
            Long museumArtworkId,
            String title,
            String description,
            String imageUrl
    ) {
    }
}
