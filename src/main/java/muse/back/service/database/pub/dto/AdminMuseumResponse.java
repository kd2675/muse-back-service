package muse.back.service.database.pub.dto;

public record AdminMuseumResponse(
        Long museumId,
        Long artistId,
        String ownerName,
        String name,
        String description,
        boolean isPublic,
        boolean isFeatured,
        int reviewingArtworkCount,
        int visibleArtworkCount,
        int removedArtworkCount
) {
}
