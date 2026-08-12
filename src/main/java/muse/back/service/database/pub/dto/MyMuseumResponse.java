package muse.back.service.database.pub.dto;

import java.time.LocalDateTime;

public record MyMuseumResponse(
        Long museumId,
        String name,
        String description,
        boolean isPublic,
        boolean isFeatured,
        int artworkCount,
        String publishStatus,
        Long coverArtworkId,
        LocalDateTime openingAt,
        String curatorNote,
        String layoutPreset,
        String lightingPreset
) {
    public MyMuseumResponse(
            Long museumId, String name, String description, boolean isPublic, boolean isFeatured, int artworkCount
    ) {
        this(museumId, name, description, isPublic, isFeatured, artworkCount,
                isPublic ? "PUBLISHED" : "DRAFT", null, null, null, "SALON", "WARM");
    }
}
