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
        LocalDateTime createdAt,
        int sortOrder,
        String roomLabel,
        int focalX,
        int focalY,
        String audioUrl,
        String audioTranscript,
        String lightingPreset
) {
    public MyMuseumArtworkResponse(
            Long museumArtworkId, Long museumId, String title, String description, String fileName,
            String imageUrl, String moderationStatus, LocalDateTime createdAt
    ) {
        this(museumArtworkId, museumId, title, description, fileName, imageUrl, moderationStatus, createdAt,
                0, null, 50, 50, null, null, "WARM");
    }
}
