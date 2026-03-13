package muse.back.service.database.pub.dto;

public record MyMuseumArtworkCreateRequest(
        String title,
        String description,
        String fileName
) {
}
