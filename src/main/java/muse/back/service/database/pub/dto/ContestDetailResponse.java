package muse.back.service.database.pub.dto;

import java.util.List;

public record ContestDetailResponse(
        Long id,
        String theme,
        String description,
        String period,
        int entryFee,
        int prizePool,
        int daysLeft,
        String status,
        int participationCount,
        List<String> rules
) {
}
