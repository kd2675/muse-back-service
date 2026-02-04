package muse.back.service.database.pub.dto;

public record ContestSummaryResponse(
        Long id,
        String theme,
        String period,
        int entryFee,
        int prizePool,
        int daysLeft,
        String status
) {
}
