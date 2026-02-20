package muse.back.service.database.pub.dto;

import java.time.LocalDateTime;

public record ContestSummaryResponse(
        Long id,
        String theme,
        String period,
        int entryFee,
        int prizePool,
        int daysLeft,
        String status,
        String phase,
        LocalDateTime submissionStartAt,
        LocalDateTime submissionEndAt,
        LocalDateTime votingStartAt,
        LocalDateTime votingEndAt
) {
}
