package muse.back.service.database.pub.dto;

public record MyMuseumCreateRequest(
        String name,
        String description,
        Boolean isPublic
) {
}
