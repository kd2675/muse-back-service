package muse.back.service.feature.profile.biz;

import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.ProfileSummaryResponse;
import muse.back.service.database.pub.entity.ProfileArtist;
import muse.back.service.database.pub.entity.ProfileAward;
import muse.back.service.database.pub.entity.ProfilePortfolio;
import muse.back.service.database.pub.entity.ProfileStat;
import muse.back.service.database.pub.repository.ProfileArtistRepository;
import muse.back.service.database.pub.repository.ProfileAwardRepository;
import muse.back.service.database.pub.repository.ProfilePortfolioRepository;
import muse.back.service.database.pub.repository.ProfileStatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileArtistRepository profileArtistRepository;
    private final ProfileStatRepository profileStatRepository;
    private final ProfilePortfolioRepository profilePortfolioRepository;
    private final ProfileAwardRepository profileAwardRepository;

    public ProfileSummaryResponse getProfileSummary(Long userId) {
        ProfileArtist artist = profileArtistRepository.findByUserId(userId)
                .orElseGet(() -> fallbackArtist(userId));

        ProfileStat stat = profileStatRepository.findByArtistId(artist.getArtistId())
                .orElseGet(() -> new ProfileStat(
                        artist.getArtistId(),
                        0,
                        0,
                        0,
                        0
                ));

        List<ProfileSummaryResponse.PortfolioItem> portfolio = profilePortfolioRepository
                .findByArtistIdOrderByPortfolioIdAsc(artist.getArtistId())
                .stream()
                .map(this::toPortfolioItem)
                .toList();

        List<ProfileSummaryResponse.AwardItem> awards = profileAwardRepository
                .findByArtistIdOrderByAwardIdAsc(artist.getArtistId())
                .stream()
                .map(this::toAwardItem)
                .toList();

        return new ProfileSummaryResponse(
                toArtist(artist),
                toStats(stat),
                portfolio,
                awards
        );
    }

    private ProfileArtist fallbackArtist(Long userId) {
        return new ProfileArtist(
                userId,
                userId,
                "Artist " + userId,
                "프로필 초기화 전",
                "#2B2A28"
        );
    }

    @Transactional
    public ProfileSummaryResponse initializeProfile(Long userId, String userName) {
        ProfileArtist artist = profileArtistRepository.findByUserId(userId)
                .orElseGet(() -> {
                    String resolvedName =
                            (userName != null && !userName.isBlank())
                                    ? userName
                                    : "Artist " + userId;
                    ProfileArtist created = new ProfileArtist(
                            userId,
                            userId,
                            resolvedName,
                            "새로운 아티스트",
                            "#2B2A28"
                    );
                    profileArtistRepository.save(created);
                    ProfileStat stat = new ProfileStat(
                            created.getArtistId(),
                            0,
                            0,
                            0,
                            0
                    );
                    profileStatRepository.save(stat);
                    return created;
                });

        ProfileStat stat = profileStatRepository.findByArtistId(artist.getArtistId())
                .orElseGet(() -> profileStatRepository.save(new ProfileStat(
                        artist.getArtistId(),
                        0,
                        0,
                        0,
                        0
                )));

        List<ProfileSummaryResponse.PortfolioItem> portfolio = profilePortfolioRepository
                .findByArtistIdOrderByPortfolioIdAsc(artist.getArtistId())
                .stream()
                .map(this::toPortfolioItem)
                .toList();

        List<ProfileSummaryResponse.AwardItem> awards = profileAwardRepository
                .findByArtistIdOrderByAwardIdAsc(artist.getArtistId())
                .stream()
                .map(this::toAwardItem)
                .toList();

        return new ProfileSummaryResponse(
                toArtist(artist),
                toStats(stat),
                portfolio,
                awards
        );
    }

    private ProfileSummaryResponse.Artist toArtist(ProfileArtist artist) {
        return new ProfileSummaryResponse.Artist(
                artist.getArtistId(),
                artist.getName(),
                artist.getTagline(),
                artist.getProfileColor()
        );
    }

    private ProfileSummaryResponse.Stats toStats(ProfileStat stat) {
        return new ProfileSummaryResponse.Stats(
                stat.getTotalWorks(),
                stat.getTotalAwards(),
                stat.getTotalEarnings(),
                stat.getFollowers()
        );
    }

    private ProfileSummaryResponse.PortfolioItem toPortfolioItem(ProfilePortfolio portfolio) {
        return new ProfileSummaryResponse.PortfolioItem(
                portfolio.getPortfolioId(),
                portfolio.getTitle(),
                portfolio.getCategory(),
                portfolio.getColorFrom(),
                portfolio.getColorTo()
        );
    }

    private ProfileSummaryResponse.AwardItem toAwardItem(ProfileAward award) {
        return new ProfileSummaryResponse.AwardItem(
                award.getAwardId(),
                award.getContest(),
                award.getRankLabel(),
                award.getPrize(),
                award.getPeriod()
        );
    }
}
