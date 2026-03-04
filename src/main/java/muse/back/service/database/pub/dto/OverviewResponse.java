package muse.back.service.database.pub.dto;

import java.util.List;

public record OverviewResponse(
        List<MuseumCard> featuredMuseums,
        List<ContestSummaryResponse> contests
) {
    public record MuseumCard(
            Long museumId,
            String name,
            String ownerName,
            int artworkCount,
            String coverImageUrl
    ) {}
}
