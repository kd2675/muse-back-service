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
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;
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
        HomeHero heroEntity = homeHeroRepository.findTopByOrderByHomeHeroIdDesc()
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Home hero not configured"));
        HomeResponse.Hero hero = new HomeResponse.Hero(
                heroEntity.getBadge(),
                heroEntity.getHeadline(),
                heroEntity.getSubheadline(),
                heroEntity.getDescription()
        );

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

        List<HomeResponse.CategoryCard> categories = galleryCategoryRepository
                .findAllByOrderByItemCountDesc()
                .stream()
                .map(this::toHomeCategoryCard)
                .toList();

        List<HomeResponse.ContestCard> contests = contestRepository
                .findByStatusOrderByDaysLeftAsc("ACTIVE")
                .stream()
                .map(this::toHomeContestCard)
                .toList();

        return new HomeResponse(hero, todaysPick, categories, contests);
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

    private HomeResponse.CategoryCard toHomeCategoryCard(GalleryCategory category) {
        return new HomeResponse.CategoryCard(
                category.getCategoryKey(),
                category.getTitle(),
                category.getDescription(),
                category.getItemCount(),
                category.getColorFrom(),
                category.getColorTo()
        );
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
