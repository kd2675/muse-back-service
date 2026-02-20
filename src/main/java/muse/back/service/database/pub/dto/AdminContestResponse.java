package muse.back.service.database.pub.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdminContestResponse(
        Long id,
        String theme,
        String description,
        String period,
        int entryFee,
        int prizePool,
        int daysLeft,
        String status,
        String phase,
        LocalDateTime submissionStartAt,
        LocalDateTime submissionEndAt,
        LocalDateTime votingStartAt,
        LocalDateTime votingEndAt,
        int participationCount,
        List<String> rules
) {
}
