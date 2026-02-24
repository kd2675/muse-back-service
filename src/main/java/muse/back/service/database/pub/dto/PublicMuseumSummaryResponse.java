package muse.back.service.database.pub.dto;

public record PublicMuseumSummaryResponse(
        Long museumId,
        String name,
        String description,
        String ownerName,
        boolean isFeatured,
        int artworkCount,
        String coverImageUrl
) {
}
