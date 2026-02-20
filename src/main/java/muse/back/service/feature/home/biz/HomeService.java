package muse.back.service.feature.home.biz;

import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.HomeResponse;
import muse.back.service.database.pub.entity.Artwork;
import muse.back.service.database.pub.entity.Contest;
import muse.back.service.database.pub.entity.GalleryCategory;
import muse.back.service.database.pub.entity.HomeHero;
import muse.back.service.database.pub.entity.HomePick;
import muse.back.service.database.pub.repository.ArtworkRepository;
import muse.back.service.database.pub.repository.ContestRepository;
import muse.back.service.database.pub.repository.GalleryCategoryRepository;
import muse.back.service.database.pub.repository.HomeHeroRepository;
import muse.back.service.database.pub.repository.HomePickRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final HomeHeroRepository homeHeroRepository;
    private final HomePickRepository homePickRepository;
    private final ArtworkRepository artworkRepository;
    private final GalleryCategoryRepository galleryCategoryRepository;
    private final ContestRepository contestRepository;

    public HomeResponse getHome() {
        HomeResponse.Hero hero = homeHeroRepository.findTopByOrderByHomeHeroIdDesc()
                .map(heroEntity -> new HomeResponse.Hero(
                        heroEntity.getBadge(),
                        heroEntity.getHeadline(),
                        heroEntity.getSubheadline(),
                        heroEntity.getDescription()
                ))
                .orElseGet(this::fallbackHero);

        List<HomePick> picks = homePickRepository.findAllByOrderBySortOrderAsc();
        Map<Long, Artwork> pickMap = artworkRepository
                .findAllById(picks.stream().map(HomePick::getArtworkId).toList())
                .stream()
                .collect(Collectors.toMap(Artwork::getArtworkId, Function.identity()));
        List<HomeResponse.ArtworkCard> todaysPick = picks.stream()
                .map(HomePick::getArtworkId)
                .map(pickMap::get)
                .filter(artwork -> artwork != null)
                .map(this::toHomeArtworkCard)
                .toList();

        Map<String, Integer> categoryItemCounts = resolveCategoryItemCountMap();
        List<HomeResponse.CategoryCard> categories = galleryCategoryRepository
                .findAllByOrderByCategoryKeyAsc()
                .stream()
                .map(category -> toHomeCategoryCard(
                        category,
                        categoryItemCounts.getOrDefault(category.getCategoryKey(), 0)
                ))
                .sorted(Comparator
                        .comparingInt(HomeResponse.CategoryCard::itemCount)
                        .reversed()
                        .thenComparing(HomeResponse.CategoryCard::title))
                .toList();

        List<HomeResponse.ContestCard> contests = contestRepository
                .findByStatusOrderByDaysLeftAsc("ACTIVE")
                .stream()
                .map(this::toHomeContestCard)
                .toList();

        return new HomeResponse(hero, todaysPick, categories, contests);
    }

    private HomeResponse.Hero fallbackHero() {
        return new HomeResponse.Hero(
                "MUSE HOME",
                "작품을 탐색하고 콘테스트를 준비하세요.",
                "홈 데이터 초기화 중입니다.",
                "홈 배너 데이터가 아직 준비되지 않았습니다. 갤러리와 콘테스트에서 최신 작품을 확인할 수 있습니다."
        );
    }

    private HomeResponse.ArtworkCard toHomeArtworkCard(Artwork artwork) {
        return new HomeResponse.ArtworkCard(
                artwork.getArtworkId(),
                artwork.getTitle(),
                artwork.getArtist(),
                resolveCategoryLabel(artwork),
                artwork.getCamera(),
                artwork.getColorFrom(),
                artwork.getColorTo()
        );
    }

    private HomeResponse.CategoryCard toHomeCategoryCard(
            GalleryCategory category,
            int itemCount
    ) {
        return new HomeResponse.CategoryCard(
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

    private HomeResponse.ContestCard toHomeContestCard(Contest contest) {
        return new HomeResponse.ContestCard(
                contest.getContestId(),
                contest.getTheme(),
                contest.getPeriod(),
                contest.getEntryFee(),
                contest.getPrizePool(),
                contest.getDaysLeft()
        );
    }

    private String resolveCategoryLabel(Artwork artwork) {
        if (artwork.getCategoryLabel() != null && !artwork.getCategoryLabel().isBlank()) {
            return artwork.getCategoryLabel();
        }
        return artwork.getCategoryKey();
    }
}
