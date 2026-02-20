package muse.back.service.database.pub.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdminContestUpsertRequest(
        String theme,
        String description,
        int entryFee,
        int prizePool,
        LocalDateTime submissionStartAt,
        LocalDateTime submissionEndAt,
        LocalDateTime votingStartAt,
        LocalDateTime votingEndAt,
        String status,
        List<String> rules
) {
}
