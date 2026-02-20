package muse.back.service.database.pub.dto;

public record AdminGalleryCategoryUpdateRequest(
        String title,
        String description,
        Integer itemCount
) {
}
