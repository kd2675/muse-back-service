package muse.back.service.database.pub.dto;

public record ContestEntryCreditResponse(
        Long contestId,
        int credits,
        String status
) {
}
