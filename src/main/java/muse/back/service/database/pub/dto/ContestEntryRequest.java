package muse.back.service.database.pub.dto;

public record ContestEntryRequest(
        String title,
        String description,
        String fileName,
        String imageUrl
) {
}
