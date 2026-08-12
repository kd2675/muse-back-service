package muse.back.service.database.pub.dto;

import java.util.List;

public record PublicArtistResponse(
        Long artistId,
        String name,
        String tagline,
        String profileColor,
        long followerCount,
        int totalWorks,
        int totalAwards,
        List<Museum> museums,
        List<Award> awards
) {
    public record Museum(Long museumId, String name, String description, int artworkCount, String coverImageUrl) {}
    public record Award(Long awardId, Long contestId, String contest, String rank, String prize, String period) {}
}
