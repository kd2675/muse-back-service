package muse.back.service.database.pub.dto;

public record MuseumBookmarkResponse(
        Long museumId,
        String name,
        String ownerName,
        String coverImageUrl,
        boolean bookmarked
) {}
