package muse.back.service.database.pub.dto;

public record ContestEntryResponse(
        Long contestId,
        String entryId,
        String title,
        String description,
        String fileName,
        String imageUrl,
        String status
) {
}
