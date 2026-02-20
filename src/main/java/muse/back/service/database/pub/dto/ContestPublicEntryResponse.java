package muse.back.service.database.pub.dto;

public record ContestPublicEntryResponse(
        String entryId,
        Long contestId,
        String title,
        String imageUrl,
        String artistName,
        String status,
        String submittedAt
) {
}
