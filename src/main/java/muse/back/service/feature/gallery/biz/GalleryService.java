package muse.back.service.feature.gallery.biz;

import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.AdminGalleryCategoryResponse;
import muse.back.service.database.pub.dto.AdminGalleryCategoryCreateRequest;
import muse.back.service.database.pub.dto.AdminGalleryCategoryUpdateRequest;
import muse.back.service.database.pub.dto.AdminGalleryHighlightResponse;
import muse.back.service.database.pub.dto.AdminGalleryHighlightUpdateRequest;
import muse.back.service.database.pub.dto.AdminGalleryArtworkCreateRequest;
import muse.back.service.database.pub.dto.AdminGalleryArtworkResponse;
import muse.back.service.database.pub.dto.GalleryLobbyResponse;
import muse.back.service.database.pub.dto.GalleryCategoryResponse;
import muse.back.service.database.pub.entity.Artwork;
import muse.back.service.database.pub.entity.ArtworkAsset;
import muse.back.service.database.pub.entity.GalleryCategory;
import muse.back.service.database.pub.entity.GalleryHighlight;
import muse.back.service.database.pub.repository.ArtworkAssetRepository;
import muse.back.service.database.pub.repository.ArtworkRepository;
import muse.back.service.database.pub.repository.GalleryCategoryRepository;
import muse.back.service.database.pub.repository.GalleryHighlightRepository;
import muse.back.service.database.pub.repository.HomePickRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GalleryService {

    private final GalleryHighlightRepository galleryHighlightRepository;
    private final GalleryCategoryRepository galleryCategoryRepository;
    private final ArtworkRepository artworkRepository;
    private final ArtworkAssetRepository artworkAssetRepository;
    private final HomePickRepository homePickRepository;

    public GalleryLobbyResponse getLobby() {
        List<GalleryHighlight> highlights = galleryHighlightRepository.findAllByOrderBySortOrderAsc();
        List<Long> highlightArtworkIds = highlights.stream().map(GalleryHighlight::getArtworkId).toList();
        Map<Long, Artwork> highlightMap = artworkRepository
                .findAllById(highlightArtworkIds)
                .stream()
                .collect(Collectors.toMap(Artwork::getArtworkId, Function.identity()));
        Map<Long, ArtworkAsset> highlightAssetMap = toArtworkAssetMap(highlightArtworkIds);
        List<GalleryLobbyResponse.Hero> heroCards = highlights.stream()
                .map(highlight -> {
                    Artwork artwork = highlightMap.get(highlight.getArtworkId());
                    if (artwork == null) {
                        return null;
                    }
                    ArtworkAsset asset = highlightAssetMap.get(artwork.getArtworkId());
                    return toHighlightCard(artwork, asset);
                })
                .filter(hero -> hero != null)
                .toList();

        Map<String, Integer> categoryItemCounts = resolveCategoryItemCountMap();
        List<GalleryLobbyResponse.CategoryCard> categories = galleryCategoryRepository
                .findAllByOrderByCategoryKeyAsc()
                .stream()
                .map(category -> toLobbyCategoryCard(
                        category,
                        categoryItemCounts.getOrDefault(category.getCategoryKey(), 0)
                ))
                .sorted(Comparator
                        .comparingInt(GalleryLobbyResponse.CategoryCard::itemCount)
                        .reversed()
                        .thenComparing(GalleryLobbyResponse.CategoryCard::title))
                .toList();

        return new GalleryLobbyResponse(heroCards, categories);
    }

    public GalleryCategoryResponse getCategoryDetail(String key) {
        GalleryCategory category = galleryCategoryRepository.findById(key)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, String.format("Category not found with key: '%s'", key)));

        List<Artwork> categoryArtworks = artworkRepository.findByCategoryKeyOrderByArtworkIdAsc(key);
        Map<Long, ArtworkAsset> assetMap = toArtworkAssetMap(
                categoryArtworks.stream().map(Artwork::getArtworkId).toList()
        );
        List<GalleryCategoryResponse.ArtworkCard> artworks = categoryArtworks
                .stream()
                .map(artwork -> toCategoryArtworkCard(
                        artwork,
                        assetMap.get(artwork.getArtworkId())
                ))
                .toList();

        return new GalleryCategoryResponse(toCategoryCard(category, artworks.size()), artworks);
    }

    public List<AdminGalleryCategoryResponse> getAdminCategories() {
        Map<String, Integer> categoryItemCounts = resolveCategoryItemCountMap();
        return galleryCategoryRepository.findAllByOrderByCategoryKeyAsc()
                .stream()
                .map(category -> toAdminCategoryResponse(
                        category,
                        categoryItemCounts.getOrDefault(category.getCategoryKey(), 0)
                ))
                .toList();
    }

    @Transactional
    public AdminGalleryCategoryResponse updateAdminCategory(
            String key,
            AdminGalleryCategoryUpdateRequest request
    ) {
        GalleryCategory category = galleryCategoryRepository.findById(key)
                .orElseThrow(() -> new GeneralException(
                        Code.NOT_FOUND,
                        String.format("Category not found with key: '%s'", key)
                ));

        if (request == null) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Request body is required");
        }
        if (request.title() == null || request.title().isBlank()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Category title is required");
        }
        int actualItemCount = Math.toIntExact(artworkRepository.countByCategoryKey(key));

        category.updateAdminFields(
                request.title().trim(),
                request.description(),
                actualItemCount
        );
        galleryCategoryRepository.save(category);
        return toAdminCategoryResponse(category, actualItemCount);
    }

    @Transactional
    public AdminGalleryCategoryResponse createAdminCategory(
            AdminGalleryCategoryCreateRequest request
    ) {
        if (request == null) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Request body is required");
        }
        if (request.key() == null || request.key().isBlank()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Category key is required");
        }
        if (request.title() == null || request.title().isBlank()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Category title is required");
        }

        String key = request.key().trim().toLowerCase();
        if (galleryCategoryRepository.existsById(key)) {
            throw new GeneralException(Code.CONFLICT, String.format("Category already exists with key: '%s'", key));
        }

        GalleryCategory category = new GalleryCategory(
                key,
                request.title().trim(),
                trimToNull(request.description()),
                0
        );
        GalleryCategory saved = galleryCategoryRepository.save(category);
        return toAdminCategoryResponse(saved, 0);
    }

    @Transactional
    public void deleteAdminCategory(String key) {
        GalleryCategory category = galleryCategoryRepository.findById(key)
                .orElseThrow(() -> new GeneralException(
                        Code.NOT_FOUND,
                        String.format("Category not found with key: '%s'", key)
                ));

        long artworkCount = artworkRepository.countByCategoryKey(key);
        if (artworkCount > 0) {
            throw new GeneralException(Code.CONFLICT, "Cannot delete category with artworks. Remove artworks first.");
        }

        galleryCategoryRepository.delete(category);
    }

    public List<AdminGalleryHighlightResponse> getAdminHighlights() {
        List<GalleryHighlight> highlights = galleryHighlightRepository.findAllByOrderBySortOrderAsc();
        return toAdminHighlightResponses(highlights);
    }

    public List<AdminGalleryArtworkResponse> getAdminArtworks() {
        List<Artwork> artworks = artworkRepository.findAllByOrderByArtworkIdDesc();
        Map<Long, ArtworkAsset> assetMap = toArtworkAssetMap(
                artworks.stream().map(Artwork::getArtworkId).toList()
        );
        return artworks
                .stream()
                .map(artwork -> toAdminArtworkResponse(artwork, assetMap.get(artwork.getArtworkId())))
                .toList();
    }

    @Transactional
    public List<AdminGalleryHighlightResponse> replaceAdminHighlights(
            AdminGalleryHighlightUpdateRequest request
    ) {
        if (request == null || request.artworkIds() == null || request.artworkIds().isEmpty()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "At least one artworkId is required");
        }
        List<Long> artworkIds = request.artworkIds();
        if (artworkIds.stream().anyMatch(artworkId -> artworkId == null)) {
            throw new GeneralException(Code.VALIDATION_ERROR, "artworkIds must not contain null");
        }
        Set<Long> uniqueArtworkIds = new LinkedHashSet<>(artworkIds);
        if (uniqueArtworkIds.size() != artworkIds.size()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Duplicate artworkIds are not allowed");
        }

        Map<Long, Artwork> artworkMap = artworkRepository.findAllById(uniqueArtworkIds)
                .stream()
                .collect(Collectors.toMap(Artwork::getArtworkId, Function.identity()));
        if (artworkMap.size() != uniqueArtworkIds.size()) {
            throw new GeneralException(Code.NOT_FOUND, "Some artworkIds do not exist");
        }

        galleryHighlightRepository.deleteAllInBatch();

        List<GalleryHighlight> newHighlights = new ArrayList<>();
        for (int index = 0; index < artworkIds.size(); index++) {
            newHighlights.add(new GalleryHighlight(artworkIds.get(index), index + 1));
        }
        List<GalleryHighlight> saved = galleryHighlightRepository.saveAll(newHighlights);
        return toAdminHighlightResponses(saved);
    }

    @Transactional
    public AdminGalleryArtworkResponse createAdminArtwork(
            AdminGalleryArtworkCreateRequest request
    ) {
        if (request == null) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Request body is required");
        }
        if (request.title() == null || request.title().isBlank()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Artwork title is required");
        }
        if (request.artist() == null || request.artist().isBlank()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Artwork artist is required");
        }
        if (request.categoryKey() == null || request.categoryKey().isBlank()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Artwork categoryKey is required");
        }
        if (request.fileName() == null || request.fileName().isBlank()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Artwork fileName is required");
        }
        if (request.imageUrl() == null || request.imageUrl().isBlank()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Artwork imageUrl is required");
        }

        String categoryKey = request.categoryKey().trim();
        GalleryCategory category = galleryCategoryRepository.findById(categoryKey)
                .orElseThrow(() -> new GeneralException(
                        Code.NOT_FOUND,
                        String.format("Category not found with key: '%s'", categoryKey)
                ));

        Artwork latestArtwork = artworkRepository.findTopByOrderByArtworkIdDesc();
        long nextArtworkId = latestArtwork == null ? 1L : latestArtwork.getArtworkId() + 1L;

        Artwork artwork = new Artwork(
                nextArtworkId,
                request.title().trim(),
                request.artist().trim(),
                categoryKey,
                category.getTitle(),
                trimToNull(request.description()),
                trimToNull(request.camera()),
                trimToNull(request.lens()),
                trimToNull(request.focalLength()),
                trimToNull(request.aperture()),
                trimToNull(request.shutterSpeed()),
                trimToNull(request.iso()),
                trimToNull(request.colorFrom()),
                trimToNull(request.colorTo())
        );
        Artwork saved = artworkRepository.save(artwork);
        ArtworkAsset asset = new ArtworkAsset(
                saved.getArtworkId(),
                request.fileName().trim(),
                request.imageUrl().trim()
        );
        artworkAssetRepository.save(asset);

        category.adjustItemCount(1);
        galleryCategoryRepository.save(category);

        return toAdminArtworkResponse(saved, asset);
    }

    @Transactional
    public void deleteAdminArtwork(Long artworkId) {
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new GeneralException(
                        Code.NOT_FOUND,
                        String.format("Artwork not found with id: '%s'", artworkId)
                ));

        galleryHighlightRepository.deleteByArtworkId(artworkId);
        homePickRepository.deleteByArtworkId(artworkId);
        artworkAssetRepository.deleteByArtworkId(artworkId);
        artworkRepository.delete(artwork);

        String categoryKey = artwork.getCategoryKey();
        if (categoryKey != null && !categoryKey.isBlank()) {
            galleryCategoryRepository.findById(categoryKey)
                    .ifPresent(category -> {
                        category.adjustItemCount(-1);
                        galleryCategoryRepository.save(category);
                    });
        }
    }

    private GalleryLobbyResponse.Hero toHighlightCard(Artwork artwork, ArtworkAsset asset) {
        return new GalleryLobbyResponse.Hero(
                artwork.getArtworkId(),
                artwork.getTitle(),
                artwork.getArtist(),
                resolveCategoryLabel(artwork),
                resolveImageUrl(asset),
                artwork.getColorFrom(),
                artwork.getColorTo()
        );
    }

    private GalleryLobbyResponse.CategoryCard toLobbyCategoryCard(
            GalleryCategory category,
            int itemCount
    ) {
        return new GalleryLobbyResponse.CategoryCard(
                category.getCategoryKey(),
                category.getTitle(),
                category.getDescription(),
                itemCount
        );
    }

    private GalleryCategoryResponse.Category toCategoryCard(GalleryCategory category, int itemCount) {
        return new GalleryCategoryResponse.Category(
                category.getCategoryKey(),
                category.getTitle(),
                category.getDescription(),
                itemCount
        );
    }

    private AdminGalleryCategoryResponse toAdminCategoryResponse(
            GalleryCategory category,
            int itemCount
    ) {
        return new AdminGalleryCategoryResponse(
                category.getCategoryKey(),
                category.getTitle(),
                category.getDescription(),
                itemCount
        );
    }

    private Map<String, Integer> resolveCategoryItemCountMap() {
        return artworkRepository.findCategoryArtworkCounts()
                .stream()
                .collect(Collectors.toMap(
                        ArtworkRepository.CategoryArtworkCount::getCategoryKey,
                        count -> Math.toIntExact(count.getItemCount())
                ));
    }

    private AdminGalleryArtworkResponse toAdminArtworkResponse(Artwork artwork, ArtworkAsset asset) {
        return new AdminGalleryArtworkResponse(
                artwork.getArtworkId(),
                artwork.getTitle(),
                artwork.getArtist(),
                artwork.getCategoryKey(),
                resolveCategoryLabel(artwork),
                asset == null ? null : asset.getFileName(),
                resolveImageUrl(asset),
                artwork.getColorFrom(),
                artwork.getColorTo()
        );
    }

    private GalleryCategoryResponse.ArtworkCard toCategoryArtworkCard(Artwork artwork, ArtworkAsset asset) {
        return new GalleryCategoryResponse.ArtworkCard(
                artwork.getArtworkId(),
                artwork.getTitle(),
                artwork.getArtist(),
                resolveImageUrl(asset),
                artwork.getColorFrom(),
                artwork.getColorTo()
        );
    }

    private List<AdminGalleryHighlightResponse> toAdminHighlightResponses(List<GalleryHighlight> highlights) {
        Map<Long, Artwork> artworkMap = artworkRepository
                .findAllById(highlights.stream().map(GalleryHighlight::getArtworkId).toList())
                .stream()
                .collect(Collectors.toMap(Artwork::getArtworkId, Function.identity()));

        return highlights.stream()
                .sorted(java.util.Comparator.comparingInt(GalleryHighlight::getSortOrder))
                .map(highlight -> {
                    Artwork artwork = artworkMap.get(highlight.getArtworkId());
                    if (artwork == null) {
                        return new AdminGalleryHighlightResponse(
                                highlight.getArtworkId(),
                                highlight.getSortOrder(),
                                "Unknown Artwork",
                                "Unknown Artist",
                                "Unknown",
                                "#D9D9D9",
                                "#BFBFBF"
                        );
                    }
                    return new AdminGalleryHighlightResponse(
                            artwork.getArtworkId(),
                            highlight.getSortOrder(),
                            artwork.getTitle(),
                            artwork.getArtist(),
                            resolveCategoryLabel(artwork),
                            artwork.getColorFrom(),
                            artwork.getColorTo()
                    );
                })
                .toList();
    }

    private String resolveCategoryLabel(Artwork artwork) {
        if (artwork.getCategoryLabel() != null && !artwork.getCategoryLabel().isBlank()) {
            return artwork.getCategoryLabel();
        }
        return artwork.getCategoryKey();
    }

    private Map<Long, ArtworkAsset> toArtworkAssetMap(List<Long> artworkIds) {
        if (artworkIds == null || artworkIds.isEmpty()) {
            return Map.of();
        }
        return artworkAssetRepository.findAllById(artworkIds)
                .stream()
                .collect(Collectors.toMap(ArtworkAsset::getArtworkId, Function.identity()));
    }

    private String resolveImageUrl(ArtworkAsset asset) {
        return asset == null ? null : asset.getImageUrl();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
