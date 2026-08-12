package muse.back.service.database.pub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ContestEntryRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        @NotBlank @Size(max = 255) String fileName,
        @NotNull @Positive @Max(104_857_600) Long fileSizeBytes,
        @NotNull @Min(3000) Integer imageWidthPx,
        @NotNull @Min(3000) Integer imageHeightPx
) {
}
