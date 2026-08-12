package muse.back.service.feature.discovery.biz;

import lombok.RequiredArgsConstructor;
import muse.back.service.common.util.ImageFileUrlResolver;
import muse.back.service.database.pub.dto.DiscoverySearchResponse;
import muse.back.service.database.pub.entity.MuseumArtwork;
import muse.back.service.database.pub.entity.ProfileArtist;
import muse.back.service.database.pub.repository.ContestRepository;
import muse.back.service.database.pub.repository.MuseumArtworkRepository;
import muse.back.service.database.pub.repository.MuseumRepository;
import muse.back.service.database.pub.repository.ProfileArtistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscoveryService {
    private static final String VISIBLE = "VISIBLE";
    private final ProfileArtistRepository artistRepository;
    private final MuseumRepository museumRepository;
    private final MuseumArtworkRepository artworkRepository;
    private final ContestRepository contestRepository;
    private final ImageFileUrlResolver imageFileUrlResolver;

    public DiscoverySearchResponse search(String rawQuery) {
        String query = rawQuery == null ? "" : rawQuery.trim();
        if (query.length() < 2 || query.length() > 80) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Search query must be between 2 and 80 characters");
        }
        var artists = artistRepository.findTop20ByNameContainingIgnoreCaseOrderByArtistIdAsc(query);
        var museums = museumRepository.findTop20ByIsPublicTrueAndNameContainingIgnoreCaseOrderByMuseumIdDesc(query);
        var artworks = artworkRepository
                .findTop20ByModerationStatusAndTitleContainingIgnoreCaseOrderByMuseumArtworkIdDesc(VISIBLE, query);
        var contests = contestRepository.findTop20ByThemeContainingIgnoreCaseOrderByContestIdDesc(query);

        var artistIds = new java.util.HashSet<Long>();
        museums.forEach(item -> artistIds.add(item.getArtistId()));
        artworks.forEach(item -> artistIds.add(item.getArtistId()));
        Map<Long, ProfileArtist> artistMap = artistRepository.findByArtistIdIn(artistIds).stream()
                .collect(Collectors.toMap(ProfileArtist::getArtistId, Function.identity()));

        var museumIds = artworks.stream().map(MuseumArtwork::getMuseumId).collect(Collectors.toSet());
        var publicMuseumIds = museumRepository.findAllById(museumIds).stream()
                .filter(item -> item.isPublic() && "PUBLISHED".equals(item.getPublishStatus()))
                .map(item -> item.getMuseumId()).collect(Collectors.toSet());

        return new DiscoverySearchResponse(
                query,
                artists.stream().map(item -> new DiscoverySearchResponse.Artist(
                        item.getArtistId(), item.getName(), item.getTagline(), item.getProfileColor()
                )).toList(),
                museums.stream().filter(item -> "PUBLISHED".equals(item.getPublishStatus())).map(item -> {
                    var visible = artworkRepository
                            .findByMuseumIdAndModerationStatusOrderBySortOrderAscMuseumArtworkIdAsc(item.getMuseumId(), VISIBLE);
                    MuseumArtwork cover = visible.stream()
                            .filter(artwork -> artwork.getMuseumArtworkId().equals(item.getCoverArtworkId()))
                            .findFirst().orElse(visible.isEmpty() ? null : visible.get(0));
                    return new DiscoverySearchResponse.Museum(
                            item.getMuseumId(), item.getName(),
                            artistMap.get(item.getArtistId()) == null ? "Unknown Artist" : artistMap.get(item.getArtistId()).getName(),
                            cover == null ? null : imageFileUrlResolver.resolveImageUrl(cover.getFileName(), cover.getImageUrl())
                    );
                }).toList(),
                contests.stream().map(item -> new DiscoverySearchResponse.Contest(
                        item.getContestId(), item.getTheme(), item.getPeriod()
                )).toList(),
                artworks.stream().filter(item -> publicMuseumIds.contains(item.getMuseumId())).map(item -> new DiscoverySearchResponse.Artwork(
                        item.getMuseumArtworkId(), item.getMuseumId(), item.getTitle(),
                        artistMap.get(item.getArtistId()) == null ? "Unknown Artist" : artistMap.get(item.getArtistId()).getName(),
                        imageFileUrlResolver.resolveImageUrl(item.getFileName(), item.getImageUrl())
                )).toList()
        );
    }
}
