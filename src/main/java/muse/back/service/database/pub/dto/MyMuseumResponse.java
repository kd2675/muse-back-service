package muse.back.service.database.pub.dto;

public record MyMuseumResponse(
        Long museumId,
        String name,
        String description,
        boolean isPublic,
        boolean isFeatured,
        int artworkCount
) {
}
