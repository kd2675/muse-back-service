package muse.back.service.database.pub.dto;

public record ContestRankingResponse(
        int rank,
        String entryId,
        String title,
        String imageUrl,
        String artistName,
        long voteCount
) {
}
