package muse.back.service.database.pub.dto;

public record AdminGalleryArtworkResponse(
        Long artworkId,
        String title,
        String artist,
        String categoryKey,
        String categoryLabel,
        String fileName,
        String imageUrl,
        String colorFrom,
        String colorTo
) {
}
