package muse.back.service.database.pub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record MuseumViewRequest(
        Long lastArtworkId,
        @Min(0) @Max(100) int progressPercent
) {}
