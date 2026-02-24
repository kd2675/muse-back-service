package muse.back.service.database.pub.dto;

public record MyMuseumUpdateRequest(
        String name,
        String description,
        Boolean isPublic
) {
}
