package muse.back.service.database.pub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContestVoteRequest(
        @NotBlank @Size(max = 64) String entryId
) {
}
