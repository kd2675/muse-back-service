package muse.back.service.feature.home.biz;

import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.HomeResponse;
import muse.back.service.database.pub.entity.Artwork;
import muse.back.service.database.pub.entity.Contest;
import muse.back.service.database.pub.entity.HomeHero;
import muse.back.service.database.pub.entity.HomePick;
import muse.back.service.database.pub.entity.Museum;
import muse.back.service.database.pub.entity.MuseumArtwork;
import muse.back.service.database.pub.entity.ProfileArtist;
import muse.back.service.database.pub.repository.ArtworkRepository;
import muse.back.service.database.pub.repository.ContestRepository;
import muse.back.service.database.pub.repository.HomeHeroRepository;
import muse.back.service.database.pub.repository.HomePickRepository;
import muse.back.service.database.pub.repository.MuseumArtworkRepository;
import muse.back.service.database.pub.repository.MuseumRepository;
import muse.back.service.database.pub.repository.ProfileArtistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
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
    private final MuseumRepository museumRepository;
    private final MuseumArtworkRepository museumArtworkRepository;
    private final ProfileArtistRepository profileArtistRepository;
    private final ContestRepository contestRepository;

    public HomeResponse getHome() {
        LocalDateTime currentTime = LocalDateTime.now();
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

        List<Museum> featuredMuseumEntities = museumRepository.findByIsPublicTrueOrderByMuseumIdDesc()
                .stream()
                .filter(Museum::isFeatured)
                .limit(8)
                .toList();
        Map<Long, String> artistNameMap = profileArtistRepository
                .findAllById(featuredMuseumEntities.stream().map(Museum::getArtistId).toList())
                .stream()
                .collect(Collectors.toMap(ProfileArtist::getArtistId, ProfileArtist::getName));
        List<HomeResponse.MuseumCard> featuredMuseums = featuredMuseumEntities.stream()
                .map(museum -> toHomeMuseumCard(
                        museum,
                        artistNameMap.getOrDefault(museum.getArtistId(), "Unknown Artist")
                ))
                .toList();

        List<HomeResponse.ContestCard> contests = contestRepository
                .findAllByOrderByContestIdAsc()
                .stream()
                .filter(contest -> !"ENDED".equals(resolveContestPhase(contest, currentTime)))
                .sorted(Comparator.comparingInt(Contest::getDaysLeft))
                .map(this::toHomeContestCard)
                .toList();

        return new HomeResponse(hero, todaysPick, featuredMuseums, contests);
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

    private HomeResponse.MuseumCard toHomeMuseumCard(Museum museum, String ownerName) {
        List<MuseumArtwork> visibleArtworks =
                museumArtworkRepository.findByMuseumIdAndModerationStatusOrderByMuseumArtworkIdDesc(
                        museum.getMuseumId(),
                        "VISIBLE"
                );
        String coverImageUrl = visibleArtworks.isEmpty() ? null : visibleArtworks.get(0).getImageUrl();
        return new HomeResponse.MuseumCard(
                museum.getMuseumId(),
                museum.getName(),
                ownerName,
                visibleArtworks.size(),
                coverImageUrl
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

    private String resolveContestPhase(Contest contest, LocalDateTime currentTime) {
        if (contest.getSubmissionStartAt() == null
                || contest.getSubmissionEndAt() == null
                || contest.getVotingStartAt() == null
                || contest.getVotingEndAt() == null) {
            return "ENDED";
        }
        if (currentTime.isBefore(contest.getSubmissionStartAt())) {
            return "UPCOMING";
        }
        if (!currentTime.isAfter(contest.getSubmissionEndAt())) {
            return "SUBMISSION";
        }
        if (currentTime.isBefore(contest.getVotingStartAt())) {
            return "REVIEW";
        }
        if (!currentTime.isAfter(contest.getVotingEndAt())) {
            return "VOTING";
        }
        return "ENDED";
    }
}
