package muse.back.service.database.pub.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ContestResultResponse(
        Long contestId,
        String theme,
        String period,
        int prizePool,
        LocalDateTime finalizedAt,
        List<Winner> winners
) {
    public record Winner(
            int rank,
            String entryId,
            String title,
            String description,
            String imageUrl,
            Long artistId,
            String artistName,
            String prize
    ) {}
}
