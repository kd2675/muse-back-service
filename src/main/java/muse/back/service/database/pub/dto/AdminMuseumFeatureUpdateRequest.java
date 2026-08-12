package muse.back.service.database.pub.dto;

import jakarta.validation.constraints.NotNull;

public record AdminMuseumFeatureUpdateRequest(
        @NotNull Boolean featured
) {
}
