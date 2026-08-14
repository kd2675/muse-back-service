package muse.back.service.feature.gallery.biz;

import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.AdminMuseumArtworkModerationUpdateRequest;
import muse.back.service.database.pub.dto.AdminMuseumArtworkResponse;
import muse.back.service.database.pub.dto.AdminMuseumFeatureUpdateRequest;
import muse.back.service.database.pub.dto.AdminMuseumResponse;
import muse.back.service.database.pub.dto.AdminMuseumVisibilityUpdateRequest;
import muse.back.service.database.pub.dto.MyMuseumArtworkCreateRequest;
import muse.back.service.database.pub.dto.MyMuseumArtworkResponse;
import muse.back.service.database.pub.dto.MyMuseumCreateRequest;
import muse.back.service.database.pub.dto.MyMuseumResponse;
import muse.back.service.database.pub.dto.MyMuseumUpdateRequest;
import muse.back.service.database.pub.dto.MuseumArtworkReorderRequest;
import muse.back.service.database.pub.dto.MuseumArtworkUpdateRequest;
import muse.back.service.database.pub.dto.MuseumCurationUpdateRequest;
import muse.back.service.database.pub.dto.PublicMuseumDetailResponse;
import muse.back.service.database.pub.dto.PublicMuseumSummaryResponse;
import muse.back.service.database.pub.entity.Museum;
import muse.back.service.database.pub.entity.MuseumArtwork;
import muse.back.service.database.pub.entity.ProfileArtist;
import muse.back.service.database.pub.repository.MuseumArtworkRepository;
import muse.back.service.database.pub.repository.MuseumRepository;
import muse.back.service.database.pub.repository.ProfileArtistRepository;
import muse.back.service.database.pub.repository.ArtistFollowRepository;
import muse.back.service.common.util.ImageFileUrlResolver;
import muse.back.service.common.util.ImageFinalizeClient;
import muse.back.service.common.util.ImageCleanupService;
import muse.back.service.feature.notification.biz.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

import java.net.URI;
import java.util.Comparator;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MuseumService {

    private static final String MODERATION_REVIEWING = "REVIEWING";
    private static final String MODERATION_VISIBLE = "VISIBLE";
    private static final String MODERATION_REMOVED = "REMOVED";
    private static final Set<String> ADMIN_MODERATION_STATUSES = Set.of(
            MODERATION_REVIEWING,
            MODERATION_VISIBLE,
            MODERATION_REMOVED
    );
    private static final String MUSEUM_IMAGE_TARGET_DIR = "muse/gallery/artworks";
    private static final Set<String> PUBLISH_STATUSES = Set.of("DRAFT", "SCHEDULED", "PUBLISHED");
    private static final Set<String> LAYOUT_PRESETS = Set.of("SALON", "LINEAR", "IMMERSIVE");
    private static final Set<String> LIGHTING_PRESETS = Set.of("WARM", "NEUTRAL", "DRAMATIC");
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final MuseumRepository museumRepository;
    private final MuseumArtworkRepository museumArtworkRepository;
    private final ProfileArtistRepository profileArtistRepository;
    private final ImageFinalizeClient imageFinalizeClient;
    private final ImageFileUrlResolver imageFileUrlResolver;
    private final ImageCleanupService imageCleanupService;
    private final ArtistFollowRepository artistFollowRepository;
    private final NotificationService notificationService;

    public List<PublicMuseumSummaryResponse> getPublicMuseums() {
        List<Museum> museums = museumRepository.findByIsPublicTrueOrderByMuseumIdDesc()
                .stream()
                .sorted(Comparator
                        .comparing(Museum::isFeatured).reversed()
                        .thenComparing(Museum::getMuseumId, Comparator.reverseOrder()))
                .toList();
        Map<Long, String> ownerNames = resolveArtistNameMap(
                museums.stream().map(Museum::getArtistId).collect(Collectors.toSet())
        );
        Map<Long, List<MuseumArtwork>> visibleArtworkMap = loadArtworkMap(museums, MODERATION_VISIBLE);

        return museums.stream()
                .map(museum -> {
                    List<MuseumArtwork> visibleArtworks = visibleArtworkMap
                            .getOrDefault(museum.getMuseumId(), List.of());
                    MuseumArtwork cover = visibleArtworks.stream()
                            .filter(item -> item.getMuseumArtworkId().equals(museum.getCoverArtworkId()))
                            .findFirst()
                            .orElse(visibleArtworks.isEmpty() ? null : visibleArtworks.get(0));
                    String coverImageUrl = cover == null ? null : resolveArtworkImageUrl(cover);
                    return new PublicMuseumSummaryResponse(
                            museum.getMuseumId(),
                            museum.getName(),
                            museum.getDescription(),
                            ownerNames.getOrDefault(museum.getArtistId(), "Unknown Artist"),
                            museum.isFeatured(),
                            visibleArtworks.size(),
                            coverImageUrl
                    );
                })
                .toList();
    }

    public PublicMuseumDetailResponse getPublicMuseumDetail(Long museumId) {
        Museum museum = museumRepository.findById(museumId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum not found"));
        if (!museum.isPublic()) {
            throw new GeneralException(Code.FORBIDDEN, "Museum is private");
        }

        String ownerName = resolveArtistName(museum.getArtistId());
        boolean contentAvailable = museum.isContentAvailableAt(LocalDateTime.now(SERVICE_ZONE));
        List<PublicMuseumDetailResponse.Artwork> artworks = (contentAvailable
                ? museumArtworkRepository.findByMuseumIdAndModerationStatusOrderBySortOrderAscMuseumArtworkIdAsc(
                        museumId, MODERATION_VISIBLE
                )
                : List.<MuseumArtwork>of())
                .stream()
                .map(artwork -> new PublicMuseumDetailResponse.Artwork(
                        artwork.getMuseumArtworkId(),
                        artwork.getTitle(),
                        artwork.getDescription(),
                        resolveArtworkImageUrl(artwork),
                        artwork.getSortOrder(),
                        artwork.getRoomLabel(),
                        artwork.getFocalX(),
                        artwork.getFocalY(),
                        artwork.getAudioUrl(),
                        artwork.getAudioTranscript(),
                        artwork.getLightingPreset()
                ))
                .toList();

        return new PublicMuseumDetailResponse(
                museum.getMuseumId(),
                museum.getName(),
                museum.getDescription(),
                museum.getArtistId(),
                ownerName,
                museum.isFeatured(),
                museum.getPublishStatus(),
                museum.getOpeningAt(),
                museum.getCuratorNote(),
                museum.getLayoutPreset(),
                museum.getLightingPreset(),
                museum.getCoverArtworkId(),
                contentAvailable,
                artworks
        );
    }

    public List<MyMuseumResponse> getMyMuseums(String userKey) {
        Long artistId = resolveArtistId(userKey);
        List<Museum> museums = museumRepository.findByArtistIdOrderByMuseumIdDesc(artistId);
        Map<Long, Long> artworkCountMap = loadArtworkCountMap(museums);
        return museums
                .stream()
                .map(museum -> toMyMuseumResponse(
                        museum,
                        artworkCountMap.getOrDefault(museum.getMuseumId(), 0L)
                ))
                .toList();
    }

    @Transactional
    public MyMuseumResponse createMyMuseum(String userKey, MyMuseumCreateRequest request) {
        validateMuseumUpsertRequest(request == null ? null : request.name());
        Long artistId = resolveArtistId(userKey);
        Museum museum = museumRepository.save(new Museum(
                artistId,
                request.name().trim(),
                trimToNull(request.description()),
                false,
                false
        ));
        return toMyMuseumResponse(museum);
    }

    @Transactional
    public MyMuseumResponse updateMyMuseum(Long museumId, String userKey, MyMuseumUpdateRequest request) {
        validateMuseumUpsertRequest(request == null ? null : request.name());
        Long artistId = resolveArtistId(userKey);
        Museum museum = museumRepository.findByMuseumIdAndArtistId(museumId, artistId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum not found"));
        museum.updateMetadata(
                request.name().trim(),
                trimToNull(request.description())
        );
        museumRepository.save(museum);
        return toMyMuseumResponse(museum);
    }

    @Transactional
    public MyMuseumResponse updateCuration(
            Long museumId,
            String userKey,
            MuseumCurationUpdateRequest request
    ) {
        Long artistId = resolveArtistId(userKey);
        Museum museum = museumRepository.findByMuseumIdAndArtistId(museumId, artistId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum not found"));
        if (request == null) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Request body is required");
        }
        String publishStatus = normalizeEnum(request.publishStatus(), PUBLISH_STATUSES, "publishStatus");
        String previousPublishStatus = museum.getPublishStatus();
        String layoutPreset = normalizeEnum(request.layoutPreset(), LAYOUT_PRESETS, "layoutPreset");
        String lightingPreset = normalizeEnum(request.lightingPreset(), LIGHTING_PRESETS, "lightingPreset");
        if ("SCHEDULED".equals(publishStatus)
                && (request.openingAt() == null || !request.openingAt().isAfter(LocalDateTime.now(SERVICE_ZONE)))) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Scheduled museum requires a future openingAt");
        }
        long visibleCount = museumArtworkRepository.countByMuseumIdAndModerationStatus(museumId, MODERATION_VISIBLE);
        if (!"DRAFT".equals(publishStatus) && visibleCount == 0) {
            throw new GeneralException(Code.CONFLICT, "At least one visible artwork is required to publish");
        }
        if (request.coverArtworkId() != null) {
            MuseumArtwork cover = museumArtworkRepository
                    .findByMuseumArtworkIdAndMuseumId(request.coverArtworkId(), museumId)
                    .orElseThrow(() -> new GeneralException(Code.VALIDATION_ERROR, "Cover artwork not found"));
            if (!MODERATION_VISIBLE.equals(cover.getModerationStatus()) && !"DRAFT".equals(publishStatus)) {
                throw new GeneralException(Code.CONFLICT, "Cover artwork is not publicly visible");
            }
        }
        museum.updateCuration(
                publishStatus,
                request.coverArtworkId(),
                request.openingAt(),
                trimToNull(request.curatorNote()),
                layoutPreset,
                lightingPreset
        );
        Museum saved = museumRepository.save(museum);
        if (!publishStatus.equals(previousPublishStatus) && !"DRAFT".equals(publishStatus)) {
            String message = "SCHEDULED".equals(publishStatus)
                    ? museum.getName() + " 전시의 오픈 일정이 공개되었습니다."
                    : museum.getName() + " 전시가 새롭게 공개되었습니다.";
            artistFollowRepository.findByFollowedArtistId(artistId).forEach(follow -> notificationService.create(
                    follow.getFollowerArtistId(),
                    "MUSEUM_OPENING",
                    "팔로우한 작가의 새 전시",
                    message,
                    "/gallery/museums/" + museumId,
                    "MUSEUM_OPENING:" + museumId + ":" + publishStatus
            ));
        }
        return toMyMuseumResponse(saved);
    }

    @Transactional
    public void deleteMyMuseum(Long museumId, String userKey) {
        Long artistId = resolveArtistId(userKey);
        Museum museum = museumRepository.findByMuseumIdAndArtistId(museumId, artistId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum not found"));
        museumArtworkRepository.findByMuseumIdOrderByMuseumArtworkIdDesc(museumId)
                .forEach(artwork -> imageCleanupService.enqueue(
                        artwork.getFileName(),
                        "MUSEUM_DELETED"
                ));
        museumArtworkRepository.deleteByMuseumId(museumId);
        museumRepository.delete(museum);
    }

    public List<MyMuseumArtworkResponse> getMyMuseumArtworks(Long museumId, String userKey) {
        Long artistId = resolveArtistId(userKey);
        requireMuseumOwner(museumId, artistId);
        return museumArtworkRepository.findByMuseumIdOrderBySortOrderAscMuseumArtworkIdAsc(museumId)
                .stream()
                .map(this::toMyMuseumArtworkResponse)
                .toList();
    }

    @Transactional
    public MyMuseumArtworkResponse createMyMuseumArtwork(
            Long museumId,
            String userKey,
            MyMuseumArtworkCreateRequest request
    ) {
        Long artistId = resolveArtistId(userKey);
        requireMuseumOwner(museumId, artistId);
        validateMuseumArtworkCreateRequest(request);
        ImageFinalizeClient.FinalizedImage finalizedImage = imageFinalizeClient.finalizeImage(
                request.fileName(),
                MUSEUM_IMAGE_TARGET_DIR
        );
        String finalizedImageFileName = finalizedImage.fileName();
        try {
            MuseumArtwork artwork = museumArtworkRepository.save(new MuseumArtwork(
                    museumId,
                    artistId,
                    request.title().trim(),
                    trimToNull(request.description()),
                    finalizedImageFileName,
                    toPortableImagePath(finalizedImageFileName),
                    MODERATION_REVIEWING
            ));
            int nextSortOrder = museumArtworkRepository.findByMuseumIdOrderBySortOrderAscMuseumArtworkIdAsc(museumId)
                    .stream().mapToInt(MuseumArtwork::getSortOrder).max().orElse(-1) + 1;
            artwork.updateSortOrder(nextSortOrder);
            return toMyMuseumArtworkResponse(artwork);
        } catch (RuntimeException exception) {
            imageCleanupService.enqueueCompensation(finalizedImageFileName, "MUSEUM_ARTWORK_ROLLBACK");
            throw exception;
        }
    }

    @Transactional
    public MyMuseumArtworkResponse updateMyMuseumArtwork(
            Long museumId,
            Long museumArtworkId,
            String userKey,
            MuseumArtworkUpdateRequest request
    ) {
        Long artistId = resolveArtistId(userKey);
        requireMuseumOwner(museumId, artistId);
        if (request == null) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Request body is required");
        }
        MuseumArtwork artwork = museumArtworkRepository.findByMuseumArtworkIdAndMuseumId(museumArtworkId, museumId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum artwork not found"));
        String lightingPreset = normalizeEnum(request.lightingPreset(), LIGHTING_PRESETS, "lightingPreset");
        String audioUrl = trimToNull(request.audioUrl());
        String audioTranscript = trimToNull(request.audioTranscript());
        validateAudioGuide(audioUrl, audioTranscript);
        artwork.updateCuration(
                request.title().trim(), trimToNull(request.description()), request.sortOrder(),
                trimToNull(request.roomLabel()), request.focalX(), request.focalY(),
                audioUrl, audioTranscript, lightingPreset
        );
        return toMyMuseumArtworkResponse(museumArtworkRepository.save(artwork));
    }

    @Transactional
    public List<MyMuseumArtworkResponse> reorderMyMuseumArtworks(
            Long museumId,
            String userKey,
            MuseumArtworkReorderRequest request
    ) {
        Long artistId = resolveArtistId(userKey);
        requireMuseumOwner(museumId, artistId);
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Artwork order is required");
        }
        List<MuseumArtwork> artworks = museumArtworkRepository.findByMuseumIdOrderBySortOrderAscMuseumArtworkIdAsc(museumId);
        Map<Long, MuseumArtwork> artworkMap = artworks.stream()
                .collect(Collectors.toMap(MuseumArtwork::getMuseumArtworkId, artwork -> artwork));
        Set<Long> requestedIds = new HashSet<>();
        for (MuseumArtworkReorderRequest.Item item : request.items()) {
            if (item == null || item.museumArtworkId() == null || !requestedIds.add(item.museumArtworkId())) {
                throw new GeneralException(Code.VALIDATION_ERROR, "Artwork order contains a duplicate or null id");
            }
            MuseumArtwork artwork = artworkMap.get(item.museumArtworkId());
            if (artwork == null) {
                throw new GeneralException(Code.VALIDATION_ERROR, "Artwork does not belong to museum");
            }
            artwork.updateSortOrder(item.sortOrder());
        }
        if (requestedIds.size() != artworks.size()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Artwork order must include every museum artwork");
        }
        museumArtworkRepository.saveAll(artworks);
        return museumArtworkRepository.findByMuseumIdOrderBySortOrderAscMuseumArtworkIdAsc(museumId)
                .stream().map(this::toMyMuseumArtworkResponse).toList();
    }

    @Transactional
    public void deleteMyMuseumArtwork(Long museumId, Long museumArtworkId, String userKey) {
        Long artistId = resolveArtistId(userKey);
        Museum museum = museumRepository.findByMuseumIdAndArtistId(museumId, artistId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum not found"));
        MuseumArtwork artwork = museumArtworkRepository.findByMuseumArtworkIdAndMuseumId(museumArtworkId, museumId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum artwork not found"));
        reconcileMuseumAfterArtworkRemoval(museum, artwork);
        imageCleanupService.enqueue(artwork.getFileName(), "MUSEUM_ARTWORK_DELETED");
        museumArtworkRepository.delete(artwork);
    }

    public List<AdminMuseumResponse> getAdminMuseums() {
        List<Museum> museums = museumRepository.findAllByOrderByMuseumIdDesc();
        Map<Long, String> ownerNames = resolveArtistNameMap(
                museums.stream().map(Museum::getArtistId).collect(Collectors.toSet())
        );
        Map<Long, Map<String, Long>> statusCountMap = loadArtworkStatusCountMap(museums);
        return museums.stream()
                .map(museum -> toAdminMuseumResponse(
                        museum,
                        ownerNames.getOrDefault(museum.getArtistId(), "Unknown Artist"),
                        statusCountMap.getOrDefault(museum.getMuseumId(), Map.of())
                ))
                .toList();
    }

    @Transactional
    public AdminMuseumResponse updateAdminMuseumFeatured(Long museumId, AdminMuseumFeatureUpdateRequest request) {
        Museum museum = museumRepository.findById(museumId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum not found"));
        if (request == null || request.featured() == null) {
            throw new GeneralException(Code.VALIDATION_ERROR, "featured is required");
        }
        museum.updateFeatured(request.featured());
        museumRepository.save(museum);
        return toAdminMuseumResponse(museum);
    }

    @Transactional
    public AdminMuseumResponse updateAdminMuseumVisibility(Long museumId, AdminMuseumVisibilityUpdateRequest request) {
        Museum museum = museumRepository.findById(museumId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum not found"));
        if (request == null || request.isPublic() == null) {
            throw new GeneralException(Code.VALIDATION_ERROR, "isPublic is required");
        }
        if (request.isPublic()
                && museumArtworkRepository.countByMuseumIdAndModerationStatus(museumId, MODERATION_VISIBLE) == 0) {
            throw new GeneralException(Code.CONFLICT, "At least one visible artwork is required to publish");
        }
        museum.updateVisibility(request.isPublic());
        museumRepository.save(museum);
        return toAdminMuseumResponse(museum);
    }

    public List<AdminMuseumArtworkResponse> getAdminMuseumArtworks(Long museumId) {
        Museum museum = museumRepository.findById(museumId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum not found"));
        String ownerName = resolveArtistName(museum.getArtistId());
        return museumArtworkRepository.findByMuseumIdOrderByMuseumArtworkIdDesc(museumId)
                .stream()
                .map(artwork -> toAdminMuseumArtworkResponse(artwork, ownerName))
                .toList();
    }

    @Transactional
    public AdminMuseumArtworkResponse updateAdminMuseumArtworkModeration(
            Long museumId,
            Long museumArtworkId,
            AdminMuseumArtworkModerationUpdateRequest request
    ) {
        Museum museum = museumRepository.findById(museumId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum not found"));
        MuseumArtwork artwork = museumArtworkRepository.findByMuseumArtworkIdAndMuseumId(museumArtworkId, museumId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum artwork not found"));

        String moderationStatus = normalizeAdminModerationStatus(
                request == null ? null : request.moderationStatus()
        );
        if (MODERATION_VISIBLE.equals(artwork.getModerationStatus())
                && !MODERATION_VISIBLE.equals(moderationStatus)) {
            reconcileMuseumAfterArtworkRemoval(museum, artwork);
        }
        artwork.updateModerationStatus(moderationStatus);
        museumArtworkRepository.save(artwork);
        return toAdminMuseumArtworkResponse(artwork, resolveArtistName(museum.getArtistId()));
    }

    @Transactional
    public void deleteAdminMuseumArtwork(Long museumId, Long museumArtworkId) {
        Museum museum = museumRepository.findById(museumId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum not found"));
        MuseumArtwork artwork = museumArtworkRepository.findByMuseumArtworkIdAndMuseumId(museumArtworkId, museumId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum artwork not found"));
        reconcileMuseumAfterArtworkRemoval(museum, artwork);
        imageCleanupService.enqueue(artwork.getFileName(), "MUSEUM_ARTWORK_ADMIN_DELETED");
        museumArtworkRepository.delete(artwork);
    }

    private void reconcileMuseumAfterArtworkRemoval(Museum museum, MuseumArtwork artwork) {
        museum.clearCoverArtworkIf(artwork.getMuseumArtworkId());
        if (museum.isPublic()
                && MODERATION_VISIBLE.equals(artwork.getModerationStatus())
                && museumArtworkRepository.countByMuseumIdAndModerationStatus(
                        museum.getMuseumId(), MODERATION_VISIBLE
                ) <= 1) {
            museum.updateVisibility(false);
        }
        museumRepository.save(museum);
    }

    private void requireMuseumOwner(Long museumId, Long artistId) {
        museumRepository.findByMuseumIdAndArtistId(museumId, artistId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum not found"));
    }

    private Long resolveArtistId(String userKey) {
        return profileArtistRepository.findByUserKey(userKey)
                .map(ProfileArtist::getArtistId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Profile artist not configured"));
    }

    private String resolveArtistName(Long artistId) {
        return profileArtistRepository.findById(artistId)
                .map(ProfileArtist::getName)
                .orElse("Unknown Artist");
    }

    private Map<Long, String> resolveArtistNameMap(Set<Long> artistIds) {
        if (artistIds == null || artistIds.isEmpty()) {
            return Map.of();
        }
        return profileArtistRepository.findAllById(artistIds)
                .stream()
                .collect(Collectors.toMap(ProfileArtist::getArtistId, ProfileArtist::getName));
    }

    private Map<Long, List<MuseumArtwork>> loadArtworkMap(List<Museum> museums, String moderationStatus) {
        if (museums.isEmpty()) {
            return Map.of();
        }
        return museumArtworkRepository
                .findByMuseumIdInAndModerationStatusOrderByMuseumIdAscSortOrderAscMuseumArtworkIdAsc(
                        museums.stream().map(Museum::getMuseumId).toList(),
                        moderationStatus
                )
                .stream()
                .collect(Collectors.groupingBy(MuseumArtwork::getMuseumId));
    }

    private Map<Long, Long> loadArtworkCountMap(List<Museum> museums) {
        if (museums.isEmpty()) {
            return Map.of();
        }
        return museumArtworkRepository.findByMuseumIdIn(
                        museums.stream().map(Museum::getMuseumId).toList()
                )
                .stream()
                .collect(Collectors.groupingBy(MuseumArtwork::getMuseumId, Collectors.counting()));
    }

    private Map<Long, Map<String, Long>> loadArtworkStatusCountMap(List<Museum> museums) {
        if (museums.isEmpty()) {
            return Map.of();
        }
        return museumArtworkRepository.findByMuseumIdIn(
                        museums.stream().map(Museum::getMuseumId).toList()
                )
                .stream()
                .collect(Collectors.groupingBy(
                        MuseumArtwork::getMuseumId,
                        Collectors.groupingBy(MuseumArtwork::getModerationStatus, Collectors.counting())
                ));
    }

    private void validateMuseumUpsertRequest(String name) {
        if (name == null || name.isBlank()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Museum name is required");
        }
    }

    private void validateMuseumArtworkCreateRequest(MyMuseumArtworkCreateRequest request) {
        if (request == null) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Request body is required");
        }
        if (request.title() == null || request.title().isBlank()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Artwork title is required");
        }
        if (request.fileName() == null || request.fileName().isBlank()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Artwork fileName is required");
        }
    }

    private String normalizeAdminModerationStatus(String value) {
        if (value == null || value.isBlank()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "moderationStatus is required");
        }
        String normalized = value.trim().toUpperCase();
        if (!ADMIN_MODERATION_STATUSES.contains(normalized)) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Invalid moderationStatus");
        }
        return normalized;
    }

    private String normalizeEnum(String value, Set<String> allowed, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new GeneralException(Code.VALIDATION_ERROR, fieldName + " is required");
        }
        String normalized = value.trim().toUpperCase();
        if (!allowed.contains(normalized)) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Invalid " + fieldName);
        }
        return normalized;
    }

    private void validateAudioGuide(String audioUrl, String audioTranscript) {
        if (audioUrl == null) {
            return;
        }
        if (audioTranscript == null) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Audio transcript is required with audioUrl");
        }
        if (audioUrl.startsWith("/") && !audioUrl.startsWith("//")) {
            return;
        }
        try {
            URI uri = URI.create(audioUrl);
            String scheme = uri.getScheme();
            if (uri.getHost() != null && ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))) {
                return;
            }
        } catch (IllegalArgumentException ignored) {
            // Converted to the public validation contract below.
        }
        throw new GeneralException(Code.VALIDATION_ERROR, "audioUrl must be an HTTP(S) or service-relative URL");
    }

    private MyMuseumResponse toMyMuseumResponse(Museum museum) {
        return toMyMuseumResponse(museum, museumArtworkRepository.countByMuseumId(museum.getMuseumId()));
    }

    private MyMuseumResponse toMyMuseumResponse(Museum museum, long artworkCount) {
        return new MyMuseumResponse(
                museum.getMuseumId(),
                museum.getName(),
                museum.getDescription(),
                museum.isPublic(),
                museum.isFeatured(),
                Math.toIntExact(artworkCount),
                museum.getPublishStatus(),
                museum.getCoverArtworkId(),
                museum.getOpeningAt(),
                museum.getCuratorNote(),
                museum.getLayoutPreset(),
                museum.getLightingPreset()
        );
    }

    private AdminMuseumResponse toAdminMuseumResponse(Museum museum) {
        return toAdminMuseumResponse(
                museum,
                resolveArtistName(museum.getArtistId()),
                Map.of(
                        MODERATION_REVIEWING, museumArtworkRepository.countByMuseumIdAndModerationStatus(
                                museum.getMuseumId(), MODERATION_REVIEWING
                        ),
                        MODERATION_VISIBLE, museumArtworkRepository.countByMuseumIdAndModerationStatus(
                                museum.getMuseumId(), MODERATION_VISIBLE
                        ),
                        MODERATION_REMOVED, museumArtworkRepository.countByMuseumIdAndModerationStatus(
                                museum.getMuseumId(), MODERATION_REMOVED
                        )
                )
        );
    }

    private AdminMuseumResponse toAdminMuseumResponse(
            Museum museum,
            String ownerName,
            Map<String, Long> statusCounts
    ) {
        return new AdminMuseumResponse(
                museum.getMuseumId(),
                museum.getArtistId(),
                ownerName,
                museum.getName(),
                museum.getDescription(),
                museum.isPublic(),
                museum.isFeatured(),
                Math.toIntExact(statusCounts.getOrDefault(MODERATION_REVIEWING, 0L)),
                Math.toIntExact(statusCounts.getOrDefault(MODERATION_VISIBLE, 0L)),
                Math.toIntExact(statusCounts.getOrDefault(MODERATION_REMOVED, 0L))
        );
    }

    private MyMuseumArtworkResponse toMyMuseumArtworkResponse(MuseumArtwork artwork) {
        return new MyMuseumArtworkResponse(
                artwork.getMuseumArtworkId(),
                artwork.getMuseumId(),
                artwork.getTitle(),
                artwork.getDescription(),
                artwork.getFileName(),
                resolveArtworkImageUrl(artwork),
                artwork.getModerationStatus(),
                artwork.getCreatedAt(),
                artwork.getSortOrder(),
                artwork.getRoomLabel(),
                artwork.getFocalX(),
                artwork.getFocalY(),
                artwork.getAudioUrl(),
                artwork.getAudioTranscript(),
                artwork.getLightingPreset()
        );
    }

    private AdminMuseumArtworkResponse toAdminMuseumArtworkResponse(MuseumArtwork artwork, String ownerName) {
        return new AdminMuseumArtworkResponse(
                artwork.getMuseumArtworkId(),
                artwork.getMuseumId(),
                artwork.getArtistId(),
                ownerName,
                artwork.getTitle(),
                artwork.getDescription(),
                artwork.getFileName(),
                resolveArtworkImageUrl(artwork),
                artwork.getModerationStatus(),
                artwork.getCreatedAt()
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String resolveArtworkImageUrl(MuseumArtwork artwork) {
        return imageFileUrlResolver.resolveImageUrl(artwork.getFileName(), artwork.getImageUrl());
    }

    private String toPortableImagePath(String fileName) {
        String normalized = fileName.startsWith("/") ? fileName.substring(1) : fileName;
        return "/images/" + normalized;
    }
}
