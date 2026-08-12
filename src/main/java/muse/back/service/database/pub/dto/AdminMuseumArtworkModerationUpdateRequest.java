package muse.back.service.database.pub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminMuseumArtworkModerationUpdateRequest(
        @NotBlank @Size(max = 20) String moderationStatus
) {
}
