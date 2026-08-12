package muse.back.service.database.pub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record AdminContestUpsertRequest(
        @NotBlank @Size(max = 200) String theme,
        @Size(max = 1000) String description,
        @Positive int entryFee,
        @PositiveOrZero int prizePool,
        @NotNull LocalDateTime submissionStartAt,
        @NotNull LocalDateTime submissionEndAt,
        @NotNull LocalDateTime votingStartAt,
        @NotNull LocalDateTime votingEndAt,
        @NotEmpty @Size(max = 20) List<@NotBlank @Size(max = 255) String> rules
) {
}
