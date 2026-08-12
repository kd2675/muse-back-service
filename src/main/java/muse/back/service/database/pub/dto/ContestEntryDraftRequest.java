package muse.back.service.database.pub.dto;

import jakarta.validation.constraints.Size;

public record ContestEntryDraftRequest(
        @Size(max = 200) String title,
        @Size(max = 2000) String description
) {}
