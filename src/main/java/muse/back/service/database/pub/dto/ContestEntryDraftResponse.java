package muse.back.service.database.pub.dto;

import java.time.LocalDateTime;

public record ContestEntryDraftResponse(Long contestId, String title, String description, LocalDateTime updatedAt) {}
