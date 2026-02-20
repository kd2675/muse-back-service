package muse.back.service.database.pub.dto;

import java.util.List;

public record ArtworkDetailResponse(
        Long id,
        String title,
        String artist,
        String category,
        String description,
        String imageUrl,
        String colorFrom,
        String colorTo,
        Exif exif,
        List<RelatedWork> relatedWorks
) {
    public record Exif(
            String camera,
            String lens,
            String focalLength,
            String aperture,
            String shutterSpeed,
            String iso
    ) {}

    public record RelatedWork(
            Long id,
            String title,
            String artist,
            String imageUrl,
            String colorFrom,
            String colorTo
    ) {}
}
