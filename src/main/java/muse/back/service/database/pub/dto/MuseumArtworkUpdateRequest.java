package muse.back.service.database.pub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MuseumArtworkUpdateRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        @Min(0) int sortOrder,
        @Size(max = 80) String roomLabel,
        @Min(0) @Max(100) int focalX,
        @Min(0) @Max(100) int focalY,
        @Size(max = 500) String audioUrl,
        @Size(max = 4000) String audioTranscript,
        String lightingPreset
) {}
