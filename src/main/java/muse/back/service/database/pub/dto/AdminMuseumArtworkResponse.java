package muse.back.service.database.pub.dto;

import java.time.LocalDateTime;

public record AdminMuseumArtworkResponse(
        Long museumArtworkId,
        Long museumId,
        Long artistId,
        String ownerName,
        String title,
        String description,
        String fileName,
        String imageUrl,
        String moderationStatus,
        LocalDateTime createdAt
) {
}
