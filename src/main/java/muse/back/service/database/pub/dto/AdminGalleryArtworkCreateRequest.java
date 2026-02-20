package muse.back.service.database.pub.dto;

public record AdminGalleryArtworkCreateRequest(
        String title,
        String artist,
        String categoryKey,
        String fileName,
        String imageUrl,
        String description,
        String camera,
        String lens,
        String focalLength,
        String aperture,
        String shutterSpeed,
        String iso,
        String colorFrom,
        String colorTo
) {
}
