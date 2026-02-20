package muse.back.service.database.pub.dto;

public record AdminGalleryCategoryResponse(
        String key,
        String title,
        String description,
        int itemCount
) {
}
