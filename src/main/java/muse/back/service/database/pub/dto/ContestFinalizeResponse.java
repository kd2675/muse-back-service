package muse.back.service.database.pub.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ContestFinalizeResponse(
        Long contestId,
        String phase,
        LocalDateTime finalizedAt,
        List<Winner> winners
) {
    public record Winner(
            int rank,
            String entryId,
            String title,
            String artistName,
            long voteCount,
            int prize
    ) {
    }
}
