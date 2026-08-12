package muse.back.service.feature.gallery.biz;

import lombok.RequiredArgsConstructor;
import muse.back.service.common.util.ImageFileUrlResolver;
import muse.back.service.database.pub.dto.MuseumBookmarkResponse;
import muse.back.service.database.pub.dto.MuseumViewRequest;
import muse.back.service.database.pub.dto.MuseumViewResponse;
import muse.back.service.database.pub.entity.Museum;
import muse.back.service.database.pub.entity.MuseumArtwork;
import muse.back.service.database.pub.entity.MuseumBookmark;
import muse.back.service.database.pub.entity.MuseumViewHistory;
import muse.back.service.database.pub.repository.MuseumArtworkRepository;
import muse.back.service.database.pub.repository.MuseumBookmarkRepository;
import muse.back.service.database.pub.repository.MuseumRepository;
import muse.back.service.database.pub.repository.MuseumViewHistoryRepository;
import muse.back.service.database.pub.repository.ProfileArtistRepository;
import muse.back.service.feature.profile.biz.ArtistIdentityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GalleryEngagementService {
    private static final String VISIBLE = "VISIBLE";
    private final ArtistIdentityService artistIdentityService;
    private final MuseumRepository museumRepository;
    private final MuseumArtworkRepository artworkRepository;
    private final MuseumBookmarkRepository bookmarkRepository;
    private final MuseumViewHistoryRepository viewHistoryRepository;
    private final ProfileArtistRepository profileArtistRepository;
    private final ImageFileUrlResolver imageFileUrlResolver;

    public List<MuseumBookmarkResponse> getBookmarks(String userKey) {
        Long artistId = artistIdentityService.requireByUserKey(userKey).getArtistId();
        return bookmarkRepository.findByArtistIdOrderByMuseumBookmarkIdDesc(artistId).stream()
                .map(item -> museumRepository.findById(item.getMuseumId()).orElse(null))
                .filter(this::isPublished)
                .map(item -> toBookmarkResponse(item, true))
                .toList();
    }

    public MuseumBookmarkResponse getBookmarkStatus(String userKey, Long museumId) {
        Long artistId = artistIdentityService.requireByUserKey(userKey).getArtistId();
        Museum museum = requirePublicMuseum(museumId);
        return toBookmarkResponse(museum, bookmarkRepository.existsByArtistIdAndMuseumId(artistId, museumId));
    }

    @Transactional
    public MuseumBookmarkResponse bookmark(String userKey, Long museumId) {
        Long artistId = artistIdentityService.requireByUserKey(userKey).getArtistId();
        Museum museum = requirePublicMuseum(museumId);
        if (!bookmarkRepository.existsByArtistIdAndMuseumId(artistId, museumId)) {
            bookmarkRepository.save(new MuseumBookmark(artistId, museumId));
        }
        return toBookmarkResponse(museum, true);
    }

    @Transactional
    public MuseumBookmarkResponse removeBookmark(String userKey, Long museumId) {
        Long artistId = artistIdentityService.requireByUserKey(userKey).getArtistId();
        Museum museum = requirePublicMuseum(museumId);
        bookmarkRepository.findByArtistIdAndMuseumId(artistId, museumId).ifPresent(bookmarkRepository::delete);
        return toBookmarkResponse(museum, false);
    }

    public List<MuseumViewResponse> getHistory(String userKey) {
        Long artistId = artistIdentityService.requireByUserKey(userKey).getArtistId();
        var histories = viewHistoryRepository.findTop30ByArtistIdOrderByViewedAtDesc(artistId);
        Map<Long, Museum> museums = museumRepository.findAllById(
                histories.stream().map(MuseumViewHistory::getMuseumId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(Museum::getMuseumId, item -> item));
        return histories.stream().filter(item -> isPublished(museums.get(item.getMuseumId())))
                .map(item -> toViewResponse(museums.get(item.getMuseumId()), item)).toList();
    }

    @Transactional
    public MuseumViewResponse recordView(String userKey, Long museumId, MuseumViewRequest request) {
        Long artistId = artistIdentityService.requireByUserKey(userKey).getArtistId();
        Museum museum = requirePublicMuseum(museumId);
        if (request.lastArtworkId() != null) {
            artworkRepository.findByMuseumArtworkIdAndMuseumId(request.lastArtworkId(), museumId)
                    .orElseThrow(() -> new GeneralException(Code.VALIDATION_ERROR, "Artwork does not belong to museum"));
        }
        MuseumViewHistory history = viewHistoryRepository.findByArtistIdAndMuseumId(artistId, museumId)
                .orElseGet(() -> new MuseumViewHistory(artistId, museumId, request.lastArtworkId(), request.progressPercent()));
        history.record(request.lastArtworkId(), request.progressPercent());
        return toViewResponse(museum, viewHistoryRepository.save(history));
    }

    private Museum requirePublicMuseum(Long museumId) {
        Museum museum = museumRepository.findById(museumId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum not found"));
        if (!isPublished(museum)) {
            throw new GeneralException(Code.NOT_FOUND, "Museum not found");
        }
        return museum;
    }

    private boolean isPublished(Museum museum) {
        return museum != null && museum.isPublic() && "PUBLISHED".equals(museum.getPublishStatus());
    }

    private MuseumBookmarkResponse toBookmarkResponse(Museum museum, boolean bookmarked) {
        return new MuseumBookmarkResponse(museum.getMuseumId(), museum.getName(), ownerName(museum), cover(museum), bookmarked);
    }

    private MuseumViewResponse toViewResponse(Museum museum, MuseumViewHistory history) {
        return new MuseumViewResponse(
                museum.getMuseumId(), museum.getName(), ownerName(museum), cover(museum), history.getLastArtworkId(),
                history.getProgressPercent(), history.getViewedAt()
        );
    }

    private String ownerName(Museum museum) {
        return profileArtistRepository.findById(museum.getArtistId()).map(item -> item.getName()).orElse("Unknown Artist");
    }

    private String cover(Museum museum) {
        List<MuseumArtwork> artworks = artworkRepository
                .findByMuseumIdAndModerationStatusOrderBySortOrderAscMuseumArtworkIdAsc(museum.getMuseumId(), VISIBLE);
        MuseumArtwork cover = artworks.stream().filter(item -> item.getMuseumArtworkId().equals(museum.getCoverArtworkId()))
                .findFirst().orElse(artworks.isEmpty() ? null : artworks.get(0));
        return cover == null ? null : imageFileUrlResolver.resolveImageUrl(cover.getFileName(), cover.getImageUrl());
    }
}
