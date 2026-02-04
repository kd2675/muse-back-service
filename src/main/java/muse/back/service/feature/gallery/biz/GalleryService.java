package muse.back.service.feature.gallery.biz;

import muse.back.service.database.pub.dto.GalleryLobbyResponse;
import muse.back.service.database.pub.dto.GalleryCategoryResponse;
import muse.back.service.common.exception.MuseException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;

@Service
public class GalleryService {

    public GalleryLobbyResponse getLobby() {
        List<GalleryLobbyResponse.Hero> highlights = List.of(
                new GalleryLobbyResponse.Hero(
                        201L,
                        "Stillness of Air",
                        "Jiyoon Park",
                        "Fine Art",
                        "#1B1B1B",
                        "#C7B89A"
                ),
                new GalleryLobbyResponse.Hero(
                        202L,
                        "Golden Horizon",
                        "Noah Kim",
                        "Landscape",
                        "#4B3B2F",
                        "#E2C08D"
                ),
                new GalleryLobbyResponse.Hero(
                        203L,
                        "City Pulse",
                        "Arin Lee",
                        "Urban",
                        "#1E2A35",
                        "#6B7C93"
                )
        );

        List<GalleryLobbyResponse.CategoryCard> categories = List.of(
                new GalleryLobbyResponse.CategoryCard(
                        "nature",
                        "Nature",
                        "고요한 자연의 리듬",
                        312,
                        "#4C5B3C",
                        "#C6D19C"
                ),
                new GalleryLobbyResponse.CategoryCard(
                        "urban",
                        "Urban",
                        "도시의 질감과 빛",
                        245,
                        "#2E2E38",
                        "#BFA7A0"
                ),
                new GalleryLobbyResponse.CategoryCard(
                        "people",
                        "People",
                        "인물의 서사",
                        198,
                        "#3A2E2A",
                        "#E3B587"
                ),
                new GalleryLobbyResponse.CategoryCard(
                        "abstract",
                        "Abstract",
                        "형태의 실험",
                        154,
                        "#2B3A4A",
                        "#C7A7E5"
                ),
                new GalleryLobbyResponse.CategoryCard(
                        "fineart",
                        "Fine Art",
                        "작품성 중심",
                        221,
                        "#2E2A25",
                        "#D7C7A8"
                ),
                new GalleryLobbyResponse.CategoryCard(
                        "night",
                        "Night",
                        "밤의 색감",
                        176,
                        "#1B1D2E",
                        "#5A7AA6"
                )
        );

        return new GalleryLobbyResponse(highlights, categories);
    }

    public GalleryCategoryResponse getCategoryDetail(String key) {
        Map<String, GalleryCategoryResponse.Category> categoryMap = Map.of(
                "nature", new GalleryCategoryResponse.Category(
                        "nature", "Nature", "고요한 자연의 리듬", 312
                ),
                "urban", new GalleryCategoryResponse.Category(
                        "urban", "Urban", "도시의 질감과 빛", 245
                ),
                "people", new GalleryCategoryResponse.Category(
                        "people", "People", "인물의 서사", 198
                ),
                "abstract", new GalleryCategoryResponse.Category(
                        "abstract", "Abstract", "형태의 실험", 154
                ),
                "fineart", new GalleryCategoryResponse.Category(
                        "fineart", "Fine Art", "작품성 중심", 221
                ),
                "night", new GalleryCategoryResponse.Category(
                        "night", "Night", "밤의 색감", 176
                )
        );

        GalleryCategoryResponse.Category category = categoryMap.get(key);
        if (category == null) {
            throw new MuseException.ResourceNotFoundException("Category", "key", key);
        }

        List<GalleryCategoryResponse.ArtworkCard> artworks = List.of(
                new GalleryCategoryResponse.ArtworkCard(
                        301L,
                        category.title() + " Echo",
                        "Hanna Lee",
                        "#3C2C2C",
                        "#D9B08C"
                ),
                new GalleryCategoryResponse.ArtworkCard(
                        302L,
                        category.title() + " Layer",
                        "Minho Park",
                        "#1F2A44",
                        "#6AA1B8"
                ),
                new GalleryCategoryResponse.ArtworkCard(
                        303L,
                        category.title() + " Silence",
                        "Sora Kim",
                        "#1C1B1F",
                        "#8C6FF0"
                ),
                new GalleryCategoryResponse.ArtworkCard(
                        304L,
                        category.title() + " Frame",
                        "Yuna Cho",
                        "#2F3A2F",
                        "#F1C6B3"
                )
        );

        return new GalleryCategoryResponse(category, artworks);
    }
}
