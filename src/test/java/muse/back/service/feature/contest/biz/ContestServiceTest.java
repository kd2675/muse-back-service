package muse.back.service.feature.contest.biz;

import muse.back.service.database.pub.dto.ContestDetailResponse;
import muse.back.service.database.pub.dto.ContestEntryResponse;
import muse.back.service.database.pub.dto.ContestSummaryResponse;
import muse.back.service.database.pub.dto.AdminContestResponse;
import muse.back.service.database.pub.entity.Contest;
import muse.back.service.database.pub.entity.ContestEntry;
import muse.back.service.database.pub.entity.ContestEntryCredit;
import muse.back.service.database.pub.entity.ProfileArtist;
import muse.back.service.database.pub.entity.ProfileStat;
import muse.back.service.database.pub.repository.ContestEntryCreditRepository;
import muse.back.service.database.pub.repository.ContestEntryLedgerRepository;
import muse.back.service.database.pub.repository.ContestEntryRepository;
import muse.back.service.database.pub.repository.ContestRepository;
import muse.back.service.database.pub.repository.ContestRuleRepository;
import muse.back.service.database.pub.repository.ProfileArtistRepository;
import muse.back.service.database.pub.repository.ProfileAwardRepository;
import muse.back.service.database.pub.repository.ProfileStatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContestServiceTest {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    @Mock
    private ContestRepository contestRepository;
    @Mock
    private ContestRuleRepository contestRuleRepository;
    @Mock
    private ContestEntryRepository contestEntryRepository;
    @Mock
    private ContestEntryCreditRepository contestEntryCreditRepository;
    @Mock
    private ContestEntryLedgerRepository contestEntryLedgerRepository;
    @Mock
    private ProfileArtistRepository profileArtistRepository;
    @Mock
    private ProfileAwardRepository profileAwardRepository;
    @Mock
    private ProfileStatRepository profileStatRepository;

    @InjectMocks
    private ContestService contestService;

    @Test
    void getContestDetail_returnsSubmissionPhase_whenNowWithinSubmissionWindow() {
        Long contestId = 101L;
        Contest contest = buildSubmissionContest(contestId);

        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));
        when(contestRuleRepository.findByContestIdOrderBySortOrderAsc(contestId)).thenReturn(List.of());

        ContestDetailResponse response = contestService.getContestDetail(contestId);

        assertThat(response.phase()).isEqualTo("SUBMISSION");
    }

    @Test
    void getContestDetail_returnsReviewPhase_whenNowAfterSubmissionEndAndBeforeVotingStart() {
        Long contestId = 102L;
        Contest contest = buildReviewWindowContest(contestId);

        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));
        when(contestRuleRepository.findByContestIdOrderBySortOrderAsc(contestId)).thenReturn(List.of());

        ContestDetailResponse response = contestService.getContestDetail(contestId);

        assertThat(response.phase()).isEqualTo("REVIEW");
    }

    @Test
    void getActiveContests_sortsByPhasePriorityThenContestId() {
        Contest ended = buildEndedContest(50L);
        Contest upcoming = buildUpcomingContest(40L);
        Contest submission = buildSubmissionContest(30L);
        Contest review = buildReviewWindowContest(20L);
        Contest voting = buildVotingContest(10L);

        when(contestRepository.findAllByOrderByContestIdAsc())
                .thenReturn(List.of(ended, upcoming, submission, review, voting));

        List<ContestSummaryResponse> response = contestService.getActiveContests();

        assertThat(response)
                .extracting(ContestSummaryResponse::id)
                .containsExactly(10L, 20L, 30L, 40L, 50L);
    }

    @Test
    void getAdminContests_sortsByPhasePriorityThenContestId() {
        Contest ended = buildEndedContest(500L);
        Contest upcoming = buildUpcomingContest(400L);
        Contest submission = buildSubmissionContest(300L);
        Contest review = buildReviewWindowContest(200L);
        Contest voting = buildVotingContest(100L);

        when(contestRepository.findAllByOrderByContestIdAsc())
                .thenReturn(List.of(ended, upcoming, submission, review, voting));
        when(contestRuleRepository.findByContestIdOrderBySortOrderAsc(anyLong()))
                .thenReturn(List.of());

        List<AdminContestResponse> response = contestService.getAdminContests();

        assertThat(response)
                .extracting(AdminContestResponse::id)
                .containsExactly(100L, 200L, 300L, 400L, 500L);
    }

    @Test
    void submitEntry_throwsValidationError_whenImageResolutionTooSmall() {
        Long contestId = 102L;
        Long userId = 11L;
        Long artistId = 501L;
        Contest contest = buildSubmissionContest(contestId);

        when(profileArtistRepository.findByUserId(userId))
                .thenReturn(Optional.of(new ProfileArtist(artistId, userId, "Artist", "tag", "#2B2A28")));
        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));

        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> contestService.submitEntry(
                        contestId,
                        userId,
                        "title",
                        "description",
                        "sample.jpg",
                        "https://example.com/a.jpg",
                        1024L,
                        2999,
                        3000
                )
        );

        assertThat(exception.getErrorCode()).isEqualTo(Code.VALIDATION_ERROR);
    }

    @Test
    void submitEntry_consumesCreditAndIncreasesTotalWorks_whenValidInput() {
        Long contestId = 103L;
        Long userId = 12L;
        Long artistId = 601L;
        Contest contest = buildSubmissionContest(contestId);
        ContestEntryCredit credit = new ContestEntryCredit(artistId, contestId, 1);
        ProfileStat stat = new ProfileStat(artistId, 0, 0, 0, 0);

        when(profileArtistRepository.findByUserId(userId))
                .thenReturn(Optional.of(new ProfileArtist(artistId, userId, "Artist", "tag", "#2B2A28")));
        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));
        when(contestEntryCreditRepository.findByArtistIdAndContestIdForUpdate(artistId, contestId))
                .thenReturn(Optional.of(credit));
        when(profileStatRepository.findByArtistId(artistId)).thenReturn(Optional.of(stat));

        ContestEntryResponse response = contestService.submitEntry(
                contestId,
                userId,
                "title",
                "description",
                "sample.jpg",
                "https://example.com/a.jpg",
                4_096L,
                3000,
                3000
        );

        assertThat(response.status()).isEqualTo("SUBMITTED");
        assertThat(credit.getBalance()).isEqualTo(0);
        assertThat(stat.getTotalWorks()).isEqualTo(1);

        verify(contestEntryCreditRepository).save(credit);
        verify(profileStatRepository).save(stat);
        verify(contestEntryRepository).save(any(ContestEntry.class));
    }

    @Test
    void submitEntry_throwsForbidden_whenCreditExistsOnlyInDifferentContest() {
        Long contestId = 201L;
        Long otherContestId = 202L;
        Long userId = 21L;
        Long artistId = 801L;
        Contest contest = buildSubmissionContest(contestId);

        when(profileArtistRepository.findByUserId(userId))
                .thenReturn(Optional.of(new ProfileArtist(artistId, userId, "Artist", "tag", "#2B2A28")));
        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));
        when(contestEntryCreditRepository.findByArtistIdAndContestIdForUpdate(artistId, contestId))
                .thenReturn(Optional.empty());

        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> contestService.submitEntry(
                        contestId,
                        userId,
                        "title",
                        "description",
                        "sample.jpg",
                        "https://example.com/a.jpg",
                        4_096L,
                        3000,
                        3000
                )
        );

        assertThat(exception.getErrorCode()).isEqualTo(Code.FORBIDDEN);
        assertThat(exception.getMessage()).contains("this contest");

        verify(contestEntryCreditRepository)
                .findByArtistIdAndContestIdForUpdate(artistId, contestId);
        verify(contestEntryCreditRepository, never())
                .findByArtistIdAndContestIdForUpdate(artistId, otherContestId);
    }

    @Test
    void voteEntry_throwsConflict_whenAlreadyVotedForEntry() {
        Long contestId = 104L;
        Long userId = 13L;
        Long artistId = 701L;
        String entryId = "EN-104-001";
        Contest contest = buildVotingContest(contestId);
        ContestEntry targetEntry = new ContestEntry(
                entryId,
                999L,
                contestId,
                "entry",
                "desc",
                "sample.jpg",
                "https://example.com/e.jpg",
                "APPROVED"
        );

        when(profileArtistRepository.findByUserId(userId))
                .thenReturn(Optional.of(new ProfileArtist(artistId, userId, "Artist", "tag", "#2B2A28")));
        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));
        when(contestEntryRepository.findByEntryIdAndContestId(entryId, contestId))
                .thenReturn(Optional.of(targetEntry));
        when(contestEntryLedgerRepository.existsByArtistIdAndContestIdAndReasonAndRefId(
                artistId,
                contestId,
                "VOTE",
                "ENTRY:" + entryId
        )).thenReturn(true);

        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> contestService.voteEntry(contestId, userId, entryId)
        );

        assertThat(exception.getErrorCode()).isEqualTo(Code.CONFLICT);
    }

    @Test
    void updateAdminEntryStatus_updatesStatus_whenContestIsInReviewWindow() {
        Long contestId = 301L;
        String entryId = "EN-301-001";
        Contest contest = buildReviewWindowContest(contestId);
        ContestEntry entry = new ContestEntry(
                entryId,
                901L,
                contestId,
                "entry",
                "desc",
                "sample.jpg",
                "https://example.com/e.jpg",
                "SUBMITTED"
        );

        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));
        when(contestEntryRepository.findByEntryIdAndContestId(entryId, contestId))
                .thenReturn(Optional.of(entry));

        var response = contestService.updateAdminEntryStatus(contestId, entryId, "REVIEWING");

        assertThat(response.status()).isEqualTo("REVIEWING");
        verify(contestEntryRepository).save(entry);
    }

    @Test
    void updateAdminEntryStatus_throwsConflict_whenContestIsInSubmissionWindow() {
        Long contestId = 302L;
        String entryId = "EN-302-001";
        Contest contest = buildSubmissionContest(contestId);

        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));

        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> contestService.updateAdminEntryStatus(contestId, entryId, "APPROVED")
        );

        assertThat(exception.getErrorCode()).isEqualTo(Code.CONFLICT);
        assertThat(exception.getMessage()).contains("during review phase");
        verify(contestEntryRepository, never()).findByEntryIdAndContestId(entryId, contestId);
    }

    @Test
    void updateAdminEntryStatus_throwsConflict_whenContestIsInVotingWindow() {
        Long contestId = 303L;
        String entryId = "EN-303-001";
        Contest contest = buildVotingContest(contestId);

        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));

        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> contestService.updateAdminEntryStatus(contestId, entryId, "APPROVED")
        );

        assertThat(exception.getErrorCode()).isEqualTo(Code.CONFLICT);
        assertThat(exception.getMessage()).contains("during review phase");
        verify(contestEntryRepository, never()).findByEntryIdAndContestId(entryId, contestId);
    }

    @Test
    void finalizeContestResults_returnsEmptyWinners_whenNoEntriesToFinalize() {
        Long contestId = 401L;
        Contest contest = buildEndedContest(contestId);

        when(contestRepository.findById(contestId)).thenReturn(Optional.of(contest));
        when(contestEntryRepository.existsByContestIdAndStatusIn(
                contestId,
                Set.of("APPROVED", "REJECTED")
        )).thenReturn(false);
        when(contestEntryRepository.findByContestIdAndStatusIn(
                contestId,
                Set.of("SUBMITTED", "REVIEWING", "APPROVED")
        )).thenReturn(List.of());

        var response = contestService.finalizeContestResults(contestId);

        assertThat(response.contestId()).isEqualTo(contestId);
        assertThat(response.phase()).isEqualTo("ENDED");
        assertThat(response.winners()).isEmpty();
        verify(contestRepository, never()).save(any());
        verify(contestEntryRepository, never()).saveAll(any());
    }

    private Contest buildSubmissionContest(Long contestId) {
        LocalDateTime now = LocalDateTime.now(SERVICE_ZONE);
        return new Contest(
                contestId,
                "theme",
                "desc",
                "period",
                3000,
                100000,
                1,
                now.minusHours(1),
                now.plusHours(1),
                now.plusHours(2),
                now.plusHours(3),
                0
        );
    }

    private Contest buildVotingContest(Long contestId) {
        LocalDateTime now = LocalDateTime.now(SERVICE_ZONE);
        return new Contest(
                contestId,
                "theme",
                "desc",
                "period",
                3000,
                100000,
                1,
                now.minusDays(2),
                now.minusDays(1),
                now.minusHours(1),
                now.plusHours(1),
                0
        );
    }

    private Contest buildReviewWindowContest(Long contestId) {
        LocalDateTime now = LocalDateTime.now(SERVICE_ZONE);
        return new Contest(
                contestId,
                "theme",
                "desc",
                "period",
                3000,
                100000,
                1,
                now.minusDays(2),
                now.minusHours(1),
                now.plusHours(1),
                now.plusDays(1),
                0
        );
    }

    private Contest buildUpcomingContest(Long contestId) {
        LocalDateTime now = LocalDateTime.now(SERVICE_ZONE);
        return new Contest(
                contestId,
                "theme",
                "desc",
                "period",
                3000,
                100000,
                1,
                now.plusHours(2),
                now.plusHours(4),
                now.plusHours(6),
                now.plusHours(8),
                0
        );
    }

    private Contest buildEndedContest(Long contestId) {
        LocalDateTime now = LocalDateTime.now(SERVICE_ZONE);
        return new Contest(
                contestId,
                "theme",
                "desc",
                "period",
                3000,
                100000,
                0,
                now.minusDays(5),
                now.minusDays(4),
                now.minusDays(3),
                now.minusDays(1),
                0
        );
    }
}
