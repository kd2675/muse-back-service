package muse.back.service.database.pub.dto;

public record AdminGalleryCategoryCreateRequest(
        String key,
        String title,
        String description
) {
}
