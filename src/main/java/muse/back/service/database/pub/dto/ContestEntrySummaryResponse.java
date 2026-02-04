package muse.back.service.database.pub.dto;

public record ContestEntrySummaryResponse(
        String entryId,
        Long contestId,
        String contestTheme,
        String title,
        String imageUrl,
        String status,
        String submittedAt
) {
}
