package muse.back.service.feature.gallery.biz;

import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.GalleryLobbyResponse;
import muse.back.service.database.pub.dto.GalleryCategoryResponse;
import muse.back.service.database.pub.entity.Artwork;
import muse.back.service.database.pub.entity.GalleryCategory;
import muse.back.service.database.pub.entity.GalleryHighlight;
import muse.back.service.database.pub.repository.ArtworkRepository;
import muse.back.service.database.pub.repository.GalleryCategoryRepository;
import muse.back.service.database.pub.repository.GalleryHighlightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GalleryService {

    private final GalleryHighlightRepository galleryHighlightRepository;
    private final GalleryCategoryRepository galleryCategoryRepository;
    private final ArtworkRepository artworkRepository;

    public GalleryLobbyResponse getLobby() {
        List<GalleryHighlight> highlights = galleryHighlightRepository.findAllByOrderBySortOrderAsc();
        Map<Long, Artwork> highlightMap = artworkRepository
                .findAllById(highlights.stream().map(GalleryHighlight::getArtworkId).toList())
                .stream()
                .collect(Collectors.toMap(Artwork::getArtworkId, Function.identity()));
        List<GalleryLobbyResponse.Hero> heroCards = highlights.stream()
                .map(GalleryHighlight::getArtworkId)
                .map(highlightMap::get)
                .filter(artwork -> artwork != null)
                .map(this::toHighlightCard)
                .toList();

        List<GalleryLobbyResponse.CategoryCard> categories = galleryCategoryRepository
                .findAllByOrderByItemCountDesc()
                .stream()
                .map(this::toLobbyCategoryCard)
                .toList();

        return new GalleryLobbyResponse(heroCards, categories);
    }

    public GalleryCategoryResponse getCategoryDetail(String key) {
        GalleryCategory category = galleryCategoryRepository.findById(key)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, String.format("Category not found with key: '%s'", key)));

        List<GalleryCategoryResponse.ArtworkCard> artworks = artworkRepository
                .findByCategoryKeyOrderByArtworkIdAsc(key)
                .stream()
                .map(this::toCategoryArtworkCard)
                .toList();

        return new GalleryCategoryResponse(toCategoryCard(category), artworks);
    }

    private GalleryLobbyResponse.Hero toHighlightCard(Artwork artwork) {
        return new GalleryLobbyResponse.Hero(
                artwork.getArtworkId(),
                artwork.getTitle(),
                artwork.getArtist(),
                resolveCategoryLabel(artwork),
                artwork.getColorFrom(),
                artwork.getColorTo()
        );
    }

    private GalleryLobbyResponse.CategoryCard toLobbyCategoryCard(GalleryCategory category) {
        return new GalleryLobbyResponse.CategoryCard(
                category.getCategoryKey(),
                category.getTitle(),
                category.getDescription(),
                category.getItemCount(),
                category.getColorFrom(),
                category.getColorTo()
        );
    }

    private GalleryCategoryResponse.Category toCategoryCard(GalleryCategory category) {
        return new GalleryCategoryResponse.Category(
                category.getCategoryKey(),
                category.getTitle(),
                category.getDescription(),
                category.getItemCount()
        );
    }

    private GalleryCategoryResponse.ArtworkCard toCategoryArtworkCard(Artwork artwork) {
        return new GalleryCategoryResponse.ArtworkCard(
                artwork.getArtworkId(),
                artwork.getTitle(),
                artwork.getArtist(),
                artwork.getColorFrom(),
                artwork.getColorTo()
        );
    }

    private String resolveCategoryLabel(Artwork artwork) {
        if (artwork.getCategoryLabel() != null && !artwork.getCategoryLabel().isBlank()) {
            return artwork.getCategoryLabel();
        }
        return artwork.getCategoryKey();
    }
}
