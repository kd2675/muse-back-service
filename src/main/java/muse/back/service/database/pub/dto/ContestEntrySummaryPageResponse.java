package muse.back.service.database.pub.dto;

import java.util.List;

public record ContestEntrySummaryPageResponse(
        List<ContestEntrySummaryResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
}
