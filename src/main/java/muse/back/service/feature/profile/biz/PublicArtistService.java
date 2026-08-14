package muse.back.service.feature.profile.biz;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import muse.back.service.common.util.ImageFileUrlResolver;
import muse.back.service.database.pub.dto.FollowStatusResponse;
import muse.back.service.database.pub.dto.PublicArtistResponse;
import muse.back.service.database.pub.entity.ArtistFollow;
import muse.back.service.database.pub.entity.MuseumArtwork;
import muse.back.service.database.pub.entity.ProfileArtist;
import muse.back.service.database.pub.entity.ProfileStat;
import muse.back.service.database.pub.repository.ArtistFollowRepository;
import muse.back.service.database.pub.repository.MuseumArtworkRepository;
import muse.back.service.database.pub.repository.MuseumRepository;
import muse.back.service.database.pub.repository.ProfileAwardRepository;
import muse.back.service.database.pub.repository.ProfileStatRepository;
import muse.back.service.feature.notification.biz.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicArtistService {
    private static final String VISIBLE = "VISIBLE";
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private final ArtistIdentityService artistIdentityService;
    private final ArtistFollowRepository artistFollowRepository;
    private final ProfileStatRepository profileStatRepository;
    private final ProfileAwardRepository profileAwardRepository;
    private final MuseumRepository museumRepository;
    private final MuseumArtworkRepository museumArtworkRepository;
    private final ImageFileUrlResolver imageFileUrlResolver;
    private final NotificationService notificationService;

    public PublicArtistResponse getArtist(Long artistId) {
        ProfileArtist artist = artistIdentityService.requireById(artistId);
        ProfileStat stat = profileStatRepository.findByArtistId(artistId)
                .orElse(new ProfileStat(artistId, 0, 0, 0, 0));
        LocalDateTime now = LocalDateTime.now(SERVICE_ZONE);
        var availableMuseums = museumRepository.findByArtistIdOrderByMuseumIdDesc(artistId).stream()
                .filter(item -> item.isContentAvailableAt(now))
                .toList();
        Map<Long, List<MuseumArtwork>> visibleArtworkMap = availableMuseums.isEmpty()
                ? Map.of()
                : museumArtworkRepository
                        .findByMuseumIdInAndModerationStatusOrderByMuseumIdAscSortOrderAscMuseumArtworkIdAsc(
                                availableMuseums.stream().map(item -> item.getMuseumId()).toList(), VISIBLE
                        )
                        .stream()
                        .collect(Collectors.groupingBy(MuseumArtwork::getMuseumId));
        var museums = availableMuseums.stream()
                .map(item -> {
                    List<MuseumArtwork> artworks = visibleArtworkMap.getOrDefault(item.getMuseumId(), List.of());
                    MuseumArtwork cover = artworks.stream()
                            .filter(artwork -> artwork.getMuseumArtworkId().equals(item.getCoverArtworkId()))
                            .findFirst().orElse(artworks.isEmpty() ? null : artworks.get(0));
                    return new PublicArtistResponse.Museum(
                            item.getMuseumId(), item.getName(), item.getDescription(), artworks.size(),
                            cover == null ? null : imageFileUrlResolver.resolveImageUrl(cover.getFileName(), cover.getImageUrl())
                    );
                }).toList();
        var awards = profileAwardRepository.findByArtistIdOrderByAwardIdAsc(artistId).stream()
                .map(item -> new PublicArtistResponse.Award(
                        item.getAwardId(), item.getContestId(), item.getContest(), item.getRankLabel(),
                        item.getPrize(), item.getPeriod()
                )).toList();
        return new PublicArtistResponse(
                artist.getArtistId(), artist.getName(), artist.getTagline(), artist.getProfileColor(),
                artistFollowRepository.countByFollowedArtistId(artistId), stat.getTotalWorks(),
                stat.getTotalAwards(), museums, awards
        );
    }

    public FollowStatusResponse getFollowStatus(String userKey, Long followedArtistId) {
        Long followerArtistId = artistIdentityService.requireByUserKey(userKey).getArtistId();
        artistIdentityService.requireById(followedArtistId);
        return status(followerArtistId, followedArtistId);
    }

    @Transactional
    public FollowStatusResponse follow(String userKey, Long followedArtistId) {
        ProfileArtist follower = artistIdentityService.requireByUserKey(userKey);
        ProfileArtist followed = artistIdentityService.requireById(followedArtistId);
        if (follower.getArtistId().equals(followedArtistId)) {
            throw new GeneralException(Code.VALIDATION_ERROR, "You cannot follow yourself");
        }
        if (!artistFollowRepository.existsByFollowerArtistIdAndFollowedArtistId(follower.getArtistId(), followedArtistId)) {
            artistFollowRepository.save(new ArtistFollow(follower.getArtistId(), followedArtistId));
            notificationService.create(
                    followedArtistId, "NEW_FOLLOWER", "새로운 관람객이 찾아왔습니다.",
                    follower.getName() + " 작가가 당신의 기록을 팔로우합니다.",
                    "/artists/" + follower.getArtistId(),
                    "follow:" + follower.getArtistId()
            );
            syncFollowerCount(followedArtistId);
        }
        return status(follower.getArtistId(), followedArtistId);
    }

    @Transactional
    public FollowStatusResponse unfollow(String userKey, Long followedArtistId) {
        Long followerArtistId = artistIdentityService.requireByUserKey(userKey).getArtistId();
        artistFollowRepository.findByFollowerArtistIdAndFollowedArtistId(followerArtistId, followedArtistId)
                .ifPresent(artistFollowRepository::delete);
        syncFollowerCount(followedArtistId);
        return status(followerArtistId, followedArtistId);
    }

    private FollowStatusResponse status(Long followerArtistId, Long followedArtistId) {
        return new FollowStatusResponse(
                followedArtistId,
                artistFollowRepository.existsByFollowerArtistIdAndFollowedArtistId(followerArtistId, followedArtistId),
                artistFollowRepository.countByFollowedArtistId(followedArtistId)
        );
    }

    private void syncFollowerCount(Long artistId) {
        ProfileStat stat = profileStatRepository.findByArtistIdForUpdate(artistId)
                .orElseGet(() -> new ProfileStat(artistId, 0, 0, 0, 0));
        stat.updateFollowers((int) artistFollowRepository.countByFollowedArtistId(artistId));
        profileStatRepository.save(stat);
    }
}
