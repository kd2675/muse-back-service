package muse.back.service.database.pub.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PublicMuseumDetailResponse(
        Long museumId,
        String name,
        String description,
        Long artistId,
        String ownerName,
        boolean isFeatured,
        String publishStatus,
        LocalDateTime openingAt,
        String curatorNote,
        String layoutPreset,
        String lightingPreset,
        Long coverArtworkId,
        boolean contentAvailable,
        List<Artwork> artworks
) {
    public PublicMuseumDetailResponse(
            Long museumId, String name, String description, String ownerName, boolean isFeatured, List<Artwork> artworks
    ) {
        this(museumId, name, description, null, ownerName, isFeatured, "PUBLISHED", null, null,
                "SALON", "WARM", null, true, artworks);
    }

    public record Artwork(
            Long museumArtworkId,
            String title,
            String description,
            String imageUrl,
            int sortOrder,
            String roomLabel,
            int focalX,
            int focalY,
            String audioUrl,
            String audioTranscript,
            String lightingPreset
    ) {
        public Artwork(Long museumArtworkId, String title, String description, String imageUrl) {
            this(museumArtworkId, title, description, imageUrl, 0, null, 50, 50, null, null, "WARM");
        }
    }
}
