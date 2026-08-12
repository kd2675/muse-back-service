package muse.back.service.database.pub.dto;

import java.time.LocalDateTime;

public record MuseumViewResponse(
        Long museumId,
        String name,
        String ownerName,
        String coverImageUrl,
        Long lastArtworkId,
        int progressPercent,
        LocalDateTime viewedAt
) {}
