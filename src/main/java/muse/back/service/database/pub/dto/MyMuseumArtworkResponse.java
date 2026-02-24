package muse.back.service.database.pub.dto;

import java.time.LocalDateTime;

public record MyMuseumArtworkResponse(
        Long museumArtworkId,
        Long museumId,
        String title,
        String description,
        String fileName,
        String imageUrl,
        String moderationStatus,
        LocalDateTime createdAt
) {
}
