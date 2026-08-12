package muse.back.service.database.pub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminContestEntryStatusUpdateRequest(
        @NotBlank @Size(max = 20) String status
) {
}
