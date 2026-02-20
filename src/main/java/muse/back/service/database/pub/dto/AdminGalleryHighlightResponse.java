package muse.back.service.database.pub.dto;

public record AdminGalleryHighlightResponse(
        Long artworkId,
        int sortOrder,
        String title,
        String artist,
        String category,
        String colorFrom,
        String colorTo
) {
}
