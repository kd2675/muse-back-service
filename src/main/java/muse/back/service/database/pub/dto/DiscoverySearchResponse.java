package muse.back.service.database.pub.dto;

import java.util.List;

public record DiscoverySearchResponse(
        String query,
        List<Artist> artists,
        List<Museum> museums,
        List<Contest> contests,
        List<Artwork> artworks
) {
    public record Artist(Long artistId, String name, String tagline, String profileColor) {}
    public record Museum(Long museumId, String name, String ownerName, String coverImageUrl) {}
    public record Contest(Long contestId, String theme, String period) {}
    public record Artwork(Long museumArtworkId, Long museumId, String title, String artistName, String imageUrl) {}
}
