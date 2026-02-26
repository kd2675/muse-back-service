package muse.back.service.database.pub.dto;

import java.util.List;

public record ContestPublicEntryPageResponse(
        List<ContestPublicEntryResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        String mode
) {
}
