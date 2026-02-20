package muse.back.service.database.pub.dto;

public record ContestVoteResponse(
        Long contestId,
        String selectedEntryId,
        long selectedEntryVoteCount
) {
}
