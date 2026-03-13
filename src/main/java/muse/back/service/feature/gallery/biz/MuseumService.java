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
import muse.back.service.database.pub.dto.PublicMuseumDetailResponse;
import muse.back.service.database.pub.dto.PublicMuseumSummaryResponse;
import muse.back.service.database.pub.entity.Museum;
import muse.back.service.database.pub.entity.MuseumArtwork;
import muse.back.service.database.pub.entity.ProfileArtist;
import muse.back.service.database.pub.repository.MuseumArtworkRepository;
import muse.back.service.database.pub.repository.MuseumRepository;
import muse.back.service.database.pub.repository.ProfileArtistRepository;
import muse.back.service.common.util.ImageFileUrlResolver;
import muse.back.service.common.util.ImageFinalizeClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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

    private final MuseumRepository museumRepository;
    private final MuseumArtworkRepository museumArtworkRepository;
    private final ProfileArtistRepository profileArtistRepository;
    private final ImageFinalizeClient imageFinalizeClient;
    private final ImageFileUrlResolver imageFileUrlResolver;

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

        return museums.stream()
                .map(museum -> {
                    List<MuseumArtwork> visibleArtworks = museumArtworkRepository
                            .findByMuseumIdAndModerationStatusOrderByMuseumArtworkIdDesc(
                                    museum.getMuseumId(),
                                    MODERATION_VISIBLE
                            );
                    String coverImageUrl = visibleArtworks.isEmpty()
                            ? null
                            : imageFileUrlResolver.resolveImageUrl(visibleArtworks.get(0).getFileName());
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
        List<PublicMuseumDetailResponse.Artwork> artworks = museumArtworkRepository
                .findByMuseumIdAndModerationStatusOrderByMuseumArtworkIdDesc(museumId, MODERATION_VISIBLE)
                .stream()
                .map(artwork -> new PublicMuseumDetailResponse.Artwork(
                        artwork.getMuseumArtworkId(),
                        artwork.getTitle(),
                        artwork.getDescription(),
                        imageFileUrlResolver.resolveImageUrl(artwork.getFileName())
                ))
                .toList();

        return new PublicMuseumDetailResponse(
                museum.getMuseumId(),
                museum.getName(),
                museum.getDescription(),
                ownerName,
                museum.isFeatured(),
                artworks
        );
    }

    public List<MyMuseumResponse> getMyMuseums(String userKey) {
        Long artistId = resolveArtistId(userKey);
        return museumRepository.findByArtistIdOrderByMuseumIdDesc(artistId)
                .stream()
                .map(this::toMyMuseumResponse)
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
                request.isPublic() == null || request.isPublic(),
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
        museum.updateByOwner(
                request.name().trim(),
                trimToNull(request.description()),
                request.isPublic() == null || request.isPublic()
        );
        museumRepository.save(museum);
        return toMyMuseumResponse(museum);
    }

    @Transactional
    public void deleteMyMuseum(Long museumId, String userKey) {
        Long artistId = resolveArtistId(userKey);
        Museum museum = museumRepository.findByMuseumIdAndArtistId(museumId, artistId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum not found"));
        museumArtworkRepository.deleteByMuseumId(museumId);
        museumRepository.delete(museum);
    }

    public List<MyMuseumArtworkResponse> getMyMuseumArtworks(Long museumId, String userKey) {
        Long artistId = resolveArtistId(userKey);
        requireMuseumOwner(museumId, artistId);
        return museumArtworkRepository.findByMuseumIdOrderByMuseumArtworkIdDesc(museumId)
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
        String finalizedImageFileName = imageFinalizeClient
                .finalizeImage(request.fileName(), MUSEUM_IMAGE_TARGET_DIR)
                .fileName();

        MuseumArtwork artwork = museumArtworkRepository.save(new MuseumArtwork(
                museumId,
                artistId,
                request.title().trim(),
                trimToNull(request.description()),
                finalizedImageFileName,
                MODERATION_REVIEWING
        ));
        return toMyMuseumArtworkResponse(artwork);
    }

    @Transactional
    public void deleteMyMuseumArtwork(Long museumId, Long museumArtworkId, String userKey) {
        Long artistId = resolveArtistId(userKey);
        requireMuseumOwner(museumId, artistId);
        MuseumArtwork artwork = museumArtworkRepository.findByMuseumArtworkIdAndMuseumId(museumArtworkId, museumId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum artwork not found"));
        museumArtworkRepository.delete(artwork);
    }

    public List<AdminMuseumResponse> getAdminMuseums() {
        List<Museum> museums = museumRepository.findAllByOrderByMuseumIdDesc();
        Map<Long, String> ownerNames = resolveArtistNameMap(
                museums.stream().map(Museum::getArtistId).collect(Collectors.toSet())
        );
        return museums.stream()
                .map(museum -> new AdminMuseumResponse(
                        museum.getMuseumId(),
                        museum.getArtistId(),
                        ownerNames.getOrDefault(museum.getArtistId(), "Unknown Artist"),
                        museum.getName(),
                        museum.getDescription(),
                        museum.isPublic(),
                        museum.isFeatured(),
                        Math.toIntExact(museumArtworkRepository.countByMuseumIdAndModerationStatus(
                                museum.getMuseumId(),
                                MODERATION_REVIEWING
                        )),
                        Math.toIntExact(museumArtworkRepository.countByMuseumIdAndModerationStatus(
                                museum.getMuseumId(),
                                MODERATION_VISIBLE
                        )),
                        Math.toIntExact(museumArtworkRepository.countByMuseumIdAndModerationStatus(
                                museum.getMuseumId(),
                                MODERATION_REMOVED
                        ))
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
        artwork.updateModerationStatus(moderationStatus);
        museumArtworkRepository.save(artwork);
        return toAdminMuseumArtworkResponse(artwork, resolveArtistName(museum.getArtistId()));
    }

    @Transactional
    public void deleteAdminMuseumArtwork(Long museumId, Long museumArtworkId) {
        museumRepository.findById(museumId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum not found"));
        MuseumArtwork artwork = museumArtworkRepository.findByMuseumArtworkIdAndMuseumId(museumArtworkId, museumId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Museum artwork not found"));
        museumArtworkRepository.delete(artwork);
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

    private MyMuseumResponse toMyMuseumResponse(Museum museum) {
        return new MyMuseumResponse(
                museum.getMuseumId(),
                museum.getName(),
                museum.getDescription(),
                museum.isPublic(),
                museum.isFeatured(),
                Math.toIntExact(museumArtworkRepository.countByMuseumId(museum.getMuseumId()))
        );
    }

    private AdminMuseumResponse toAdminMuseumResponse(Museum museum) {
        return new AdminMuseumResponse(
                museum.getMuseumId(),
                museum.getArtistId(),
                resolveArtistName(museum.getArtistId()),
                museum.getName(),
                museum.getDescription(),
                museum.isPublic(),
                museum.isFeatured(),
                Math.toIntExact(museumArtworkRepository.countByMuseumIdAndModerationStatus(
                        museum.getMuseumId(),
                        MODERATION_REVIEWING
                )),
                Math.toIntExact(museumArtworkRepository.countByMuseumIdAndModerationStatus(
                        museum.getMuseumId(),
                        MODERATION_VISIBLE
                )),
                Math.toIntExact(museumArtworkRepository.countByMuseumIdAndModerationStatus(
                        museum.getMuseumId(),
                        MODERATION_REMOVED
                ))
        );
    }

    private MyMuseumArtworkResponse toMyMuseumArtworkResponse(MuseumArtwork artwork) {
        return new MyMuseumArtworkResponse(
                artwork.getMuseumArtworkId(),
                artwork.getMuseumId(),
                artwork.getTitle(),
                artwork.getDescription(),
                artwork.getFileName(),
                imageFileUrlResolver.resolveImageUrl(artwork.getFileName()),
                artwork.getModerationStatus(),
                artwork.getCreatedAt()
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
                imageFileUrlResolver.resolveImageUrl(artwork.getFileName()),
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
}
