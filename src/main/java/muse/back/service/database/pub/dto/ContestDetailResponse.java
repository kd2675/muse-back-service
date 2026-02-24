package muse.back.service.database.pub.dto;

import java.util.List;
import java.time.LocalDateTime;

public record ContestDetailResponse(
        Long id,
        String theme,
        String description,
        String period,
        int entryFee,
        int prizePool,
        int daysLeft,
        String phase,
        LocalDateTime submissionStartAt,
        LocalDateTime submissionEndAt,
        LocalDateTime votingStartAt,
        LocalDateTime votingEndAt,
        int participationCount,
        List<String> rules
) {
}
