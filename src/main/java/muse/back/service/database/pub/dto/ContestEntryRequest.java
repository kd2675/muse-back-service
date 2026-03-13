package muse.back.service.database.pub.dto;

public record ContestEntryRequest(
        String title,
        String description,
        String fileName,
        Long fileSizeBytes,
        Integer imageWidthPx,
        Integer imageHeightPx
) {
}
