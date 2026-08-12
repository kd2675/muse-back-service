package muse.back.service.feature.overview.biz;

import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.ContestSummaryResponse;
import muse.back.service.database.pub.dto.OverviewResponse;
import muse.back.service.database.pub.entity.Contest;
import muse.back.service.database.pub.entity.Museum;
import muse.back.service.database.pub.entity.MuseumArtwork;
import muse.back.service.database.pub.entity.ProfileArtist;
import muse.back.service.database.pub.repository.ContestRepository;
import muse.back.service.database.pub.repository.MuseumArtworkRepository;
import muse.back.service.database.pub.repository.MuseumRepository;
import muse.back.service.database.pub.repository.ProfileArtistRepository;
import muse.back.service.common.util.ImageFileUrlResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OverviewService {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private static final String PHASE_UPCOMING = "UPCOMING";
    private static final String PHASE_SUBMISSION = "SUBMISSION";
    private static final String PHASE_REVIEW = "REVIEW";
    private static final String PHASE_VOTING = "VOTING";
    private static final String PHASE_ENDED = "ENDED";

    private final ContestRepository contestRepository;
    private final MuseumRepository museumRepository;
    private final MuseumArtworkRepository museumArtworkRepository;
    private final ProfileArtistRepository profileArtistRepository;
    private final ImageFileUrlResolver imageFileUrlResolver;

    public OverviewResponse getOverview() {
        LocalDateTime currentTime = LocalDateTime.now(SERVICE_ZONE);

        List<Museum> featuredMuseumEntities = museumRepository.findByIsPublicTrueOrderByMuseumIdDesc()
                .stream()
                .filter(Museum::isFeatured)
                .toList();
        List<Museum> shuffledFeaturedMuseumEntities = new ArrayList<>(featuredMuseumEntities);
        Collections.shuffle(shuffledFeaturedMuseumEntities);
        List<Museum> featuredMuseumTop = shuffledFeaturedMuseumEntities.stream()
                .limit(8)
                .toList();

        Map<Long, String> artistNameMap = profileArtistRepository
                .findAllById(featuredMuseumTop.stream().map(Museum::getArtistId).toList())
                .stream()
                .collect(Collectors.toMap(ProfileArtist::getArtistId, ProfileArtist::getName, (left, right) -> left));
        Map<Long, List<MuseumArtwork>> visibleArtworkMap = loadVisibleArtworkMap(featuredMuseumTop);

        List<OverviewResponse.MuseumCard> featuredMuseums = featuredMuseumTop.stream()
                .map(museum -> toOverviewMuseumCard(
                        museum,
                        artistNameMap.getOrDefault(museum.getArtistId(), "Unknown Artist"),
                        visibleArtworkMap.getOrDefault(museum.getMuseumId(), List.of())
                ))
                .toList();

        List<ContestSummaryResponse> contests = contestRepository.findAllByOrderByContestIdAsc()
                .stream()
                .map(contest -> toOverviewContestCard(contest, currentTime))
                .filter(contest -> !PHASE_ENDED.equals(contest.phase()))
                .toList();

        return new OverviewResponse(featuredMuseums, contests);
    }

    private OverviewResponse.MuseumCard toOverviewMuseumCard(
            Museum museum,
            String ownerName,
            List<MuseumArtwork> visibleArtworks
    ) {
        String coverImageUrl = visibleArtworks.isEmpty()
                ? null
                : imageFileUrlResolver.resolveImageUrl(
                        visibleArtworks.get(0).getFileName(),
                        visibleArtworks.get(0).getImageUrl()
                );

        return new OverviewResponse.MuseumCard(
                museum.getMuseumId(),
                museum.getName(),
                ownerName,
                visibleArtworks.size(),
                coverImageUrl
        );
    }

    private Map<Long, List<MuseumArtwork>> loadVisibleArtworkMap(List<Museum> museums) {
        if (museums.isEmpty()) {
            return Map.of();
        }
        return museumArtworkRepository
                .findByMuseumIdInAndModerationStatusOrderByMuseumIdAscMuseumArtworkIdDesc(
                        museums.stream().map(Museum::getMuseumId).toList(),
                        "VISIBLE"
                )
                .stream()
                .collect(Collectors.groupingBy(MuseumArtwork::getMuseumId));
    }

    private ContestSummaryResponse toOverviewContestCard(Contest contest, LocalDateTime currentTime) {
        return new ContestSummaryResponse(
                contest.getContestId(),
                contest.getTheme(),
                contest.getPeriod(),
                contest.getEntryFee(),
                contest.getPrizePool(),
                computeDaysLeft(contest.getVotingEndAt(), currentTime),
                resolveContestPhase(contest, currentTime),
                contest.getSubmissionStartAt(),
                contest.getSubmissionEndAt(),
                contest.getVotingStartAt(),
                contest.getVotingEndAt()
        );
    }

    private String resolveContestPhase(Contest contest, LocalDateTime currentTime) {
        if (contest.getSubmissionStartAt() == null
                || contest.getSubmissionEndAt() == null
                || contest.getVotingStartAt() == null
                || contest.getVotingEndAt() == null) {
            return PHASE_ENDED;
        }
        if (currentTime.isBefore(contest.getSubmissionStartAt())) {
            return PHASE_UPCOMING;
        }
        if (!currentTime.isAfter(contest.getSubmissionEndAt())) {
            return PHASE_SUBMISSION;
        }
        if (currentTime.isBefore(contest.getVotingStartAt())) {
            return PHASE_REVIEW;
        }
        if (!currentTime.isAfter(contest.getVotingEndAt())) {
            return PHASE_VOTING;
        }
        return PHASE_ENDED;
    }

    private int computeDaysLeft(LocalDateTime votingEndAt, LocalDateTime currentTime) {
        if (votingEndAt == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(currentTime.toLocalDate(), votingEndAt.toLocalDate());
        return (int) Math.max(days, 0);
    }
}
