package muse.back.service.database.pub.dto;

public record ArtworkDetailResponse(
        Long id,
        String title,
        String artist,
        String category,
        String description,
        String colorFrom,
        String colorTo,
        Exif exif
) {
    public record Exif(
            String camera,
            String lens,
            String focalLength,
            String aperture,
            String shutterSpeed,
            String iso
    ) {}
}
