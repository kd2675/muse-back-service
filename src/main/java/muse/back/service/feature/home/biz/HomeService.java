package muse.back.service.feature.home.biz;

import muse.back.service.database.pub.dto.HomeResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeService {

    public HomeResponse getHome() {
        HomeResponse.Hero hero = new HomeResponse.Hero(
                "TODAY'S PICK",
                "경쟁과 감상이 공존하는 디지털 미술관",
                "참여형 사진 콘테스트와 영구 전시를 하나의 경험으로",
                "엄선된 작품만 전시되는 갤러리에서 오늘의 감동을 만나보세요."
        );

        List<HomeResponse.ArtworkCard> todaysPick = List.of(
                new HomeResponse.ArtworkCard(
                        1L,
                        "Glass River",
                        "Hanna Lee",
                        "Urban",
                        "Leica Q3 · 28mm",
                        "#3C2C2C",
                        "#D9B08C"
                ),
                new HomeResponse.ArtworkCard(
                        2L,
                        "Echoes of Fog",
                        "Minho Park",
                        "Nature",
                        "Canon R5 · 70mm",
                        "#1F2A44",
                        "#6AA1B8"
                ),
                new HomeResponse.ArtworkCard(
                        3L,
                        "Velvet Night",
                        "Sora Kim",
                        "Night",
                        "Sony A7 IV · 50mm",
                        "#1C1B1F",
                        "#8C6FF0"
                ),
                new HomeResponse.ArtworkCard(
                        4L,
                        "Bloomline",
                        "Yuna Cho",
                        "Macro",
                        "Fujifilm X-T5 · 80mm",
                        "#2F3A2F",
                        "#F1C6B3"
                )
        );

        List<HomeResponse.CategoryCard> categories = List.of(
                new HomeResponse.CategoryCard(
                        "nature",
                        "Nature",
                        "고요한 자연의 리듬",
                        312,
                        "#4C5B3C",
                        "#C6D19C"
                ),
                new HomeResponse.CategoryCard(
                        "urban",
                        "Urban",
                        "도시의 질감과 빛",
                        245,
                        "#2E2E38",
                        "#BFA7A0"
                ),
                new HomeResponse.CategoryCard(
                        "people",
                        "People",
                        "인물의 서사",
                        198,
                        "#3A2E2A",
                        "#E3B587"
                ),
                new HomeResponse.CategoryCard(
                        "abstract",
                        "Abstract",
                        "형태의 실험",
                        154,
                        "#2B3A4A",
                        "#C7A7E5"
                ),
                new HomeResponse.CategoryCard(
                        "fineart",
                        "Fine Art",
                        "작품성 중심",
                        221,
                        "#2E2A25",
                        "#D7C7A8"
                ),
                new HomeResponse.CategoryCard(
                        "night",
                        "Night",
                        "밤의 색감",
                        176,
                        "#1B1D2E",
                        "#5A7AA6"
                )
        );

        List<HomeResponse.ContestCard> contests = List.of(
                new HomeResponse.ContestCard(
                        101L,
                        "빛의 레이어",
                        "2026.02.01 - 2026.02.07",
                        3000,
                        420000,
                        4
                ),
                new HomeResponse.ContestCard(
                        102L,
                        "도시의 숨",
                        "2026.02.01 - 2026.02.14",
                        3000,
                        680000,
                        11
                ),
                new HomeResponse.ContestCard(
                        103L,
                        "완벽한 정적",
                        "2026.02.01 - 2026.02.28",
                        3000,
                        1250000,
                        25
                )
        );

        return new HomeResponse(hero, todaysPick, categories, contests);
    }
}
