package muse.back.service.feature.contest.biz;

import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.ContestDetailResponse;
import muse.back.service.database.pub.dto.ContestEntryCreditResponse;
import muse.back.service.database.pub.dto.ContestEntryResponse;
import muse.back.service.database.pub.dto.ContestEntrySummaryResponse;
import muse.back.service.database.pub.dto.ContestSummaryResponse;
import muse.back.service.database.pub.entity.ContestEntry;
import muse.back.service.database.pub.entity.ContestEntryCredit;
import muse.back.service.database.pub.entity.ContestEntryLedger;
import muse.back.service.database.pub.entity.Contest;
import muse.back.service.database.pub.entity.ContestRule;
import muse.back.service.database.pub.entity.ProfileArtist;
import muse.back.service.database.pub.repository.ContestEntryCreditRepository;
import muse.back.service.database.pub.repository.ContestEntryLedgerRepository;
import muse.back.service.database.pub.repository.ContestEntryRepository;
import muse.back.service.database.pub.repository.ContestRepository;
import muse.back.service.database.pub.repository.ContestRuleRepository;
import muse.back.service.database.pub.repository.ProfileArtistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestService {

    private static final DateTimeFormatter SUBMITTED_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ContestRepository contestRepository;
    private final ContestRuleRepository contestRuleRepository;
    private final ContestEntryRepository contestEntryRepository;
    private final ContestEntryCreditRepository contestEntryCreditRepository;
    private final ContestEntryLedgerRepository contestEntryLedgerRepository;
    private final ProfileArtistRepository profileArtistRepository;

    public List<ContestSummaryResponse> getActiveContests() {
        return contestRepository.findByStatusOrderByDaysLeftAsc("ACTIVE")
                .stream()
                .map(this::toContestSummary)
                .toList();
    }

    public ContestDetailResponse getContestDetail(Long id) {
        Contest contest = contestRepository.findById(id)
                .orElseThrow(() -> new GeneralException(
                        Code.NOT_FOUND,
                        String.format("Contest not found with id: '%s'", id)
                ));
        List<String> rules = contestRuleRepository
                .findByContestIdOrderBySortOrderAsc(id)
                .stream()
                .map(ContestRule::getRuleText)
                .toList();
        return new ContestDetailResponse(
                contest.getContestId(),
                contest.getTheme(),
                contest.getDescription(),
                contest.getPeriod(),
                contest.getEntryFee(),
                contest.getPrizePool(),
                contest.getDaysLeft(),
                contest.getStatus(),
                contest.getParticipationCount(),
                rules
        );
    }

    @Transactional
    public ContestEntryResponse submitEntry(
            Long contestId,
            Long userId,
            String title,
            String description,
            String fileName,
            String imageUrl
    ) {
        Long artistId = resolveArtistId(userId);
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new GeneralException(
                        Code.NOT_FOUND,
                        String.format("Contest not found with id: '%s'", contestId)
                ));
        if (!"ACTIVE".equals(contest.getStatus())) {
            throw new GeneralException(Code.CONFLICT, "Contest is not active");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Image URL is required");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "File name is required");
        }
        String entryId = "EN-" + contestId + "-" + System.currentTimeMillis();
        consumeEntryCredit(contestId, artistId);

        ContestEntry entry = new ContestEntry(
                entryId,
                artistId,
                contestId,
                title,
                description,
                fileName,
                imageUrl,
                "SUBMITTED"
        );
        contest.increaseParticipationCount();
        contestRepository.save(contest);
        contestEntryRepository.save(entry);
        contestEntryLedgerRepository.save(new ContestEntryLedger(
                artistId,
                contestId,
                -1,
                "SUBMIT",
                entryId
        ));

        return new ContestEntryResponse(
                contestId,
                entryId,
                title,
                description,
                fileName,
                imageUrl,
                "SUBMITTED"
        );
    }

    @Transactional
    public ContestEntryCreditResponse purchaseEntryCredit(Long contestId, Long userId) {
        Long artistId = resolveArtistId(userId);
        ContestDetailResponse detail = getContestDetail(contestId);
        if (!"ACTIVE".equals(detail.status())) {
            throw new GeneralException(Code.CONFLICT, "Contest is not active");
        }
        ContestEntryCredit credit = getOrCreateEntryCreditForUpdate(contestId, artistId);
        credit.increase(1);
        contestEntryCreditRepository.save(credit);
        contestEntryLedgerRepository.save(new ContestEntryLedger(
                artistId,
                contestId,
                1,
                "PURCHASE",
                null
        ));
        int credits = credit.getBalance();
        return new ContestEntryCreditResponse(contestId, credits, "AVAILABLE");
    }

    public ContestEntryCreditResponse getEntryCreditStatus(Long contestId, Long userId) {
        Long artistId = resolveArtistId(userId);
        getContestDetail(contestId);
        int credits = contestEntryCreditRepository
                .findByArtistIdAndContestId(artistId, contestId)
                .map(ContestEntryCredit::getBalance)
                .orElse(0);
        return new ContestEntryCreditResponse(
                contestId,
                credits,
                credits > 0 ? "AVAILABLE" : "NONE"
        );
    }

    private ContestEntryCredit getOrCreateEntryCreditForUpdate(Long contestId, Long artistId) {
        return contestEntryCreditRepository
                .findByArtistIdAndContestIdForUpdate(artistId, contestId)
                .orElseGet(() -> new ContestEntryCredit(artistId, contestId, 0));
    }

    private void consumeEntryCredit(Long contestId, Long artistId) {
        ContestEntryCredit credit = getOrCreateEntryCreditForUpdate(contestId, artistId);
        int current = credit.getBalance();
        if (current <= 0) {
            throw new GeneralException(Code.FORBIDDEN, "Entry credit required");
        }
        credit.decrease(1);
        contestEntryCreditRepository.save(credit);
    }

    public List<ContestEntrySummaryResponse> getMyEntries(Long userId) {
        Long artistId = resolveArtistId(userId);
        return contestEntryRepository.findByArtistIdOrderByCreateDateDesc(artistId)
                .stream()
                .map(this::toEntrySummary)
                .toList();
    }

    @Transactional
    public void deleteEntry(String entryId, Long userId) {
        Long artistId = resolveArtistId(userId);
        ContestEntry entry = contestEntryRepository.findByEntryIdAndArtistId(entryId, artistId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, String.format("Entry not found with id: '%s'", entryId)));

        // 이미 심사 중/완료된 출품은 삭제를 제한
        if (!"SUBMITTED".equals(entry.getStatus())) {
            throw new GeneralException(Code.CONFLICT, "Only submitted entries can be deleted");
        }

        Long contestId = entry.getContestId();
        contestEntryRepository.delete(entry);

        // 출품 취소 시 참여 카운트를 되돌림
        contestRepository.findById(contestId).ifPresent(contest -> {
            contest.decreaseParticipationCount();
            contestRepository.save(contest);
        });

        // 출품 취소 시 출품권 1개 환불
        ContestEntryCredit credit = getOrCreateEntryCreditForUpdate(contestId, artistId);
        credit.increase(1);
        contestEntryCreditRepository.save(credit);

        contestEntryLedgerRepository.save(new ContestEntryLedger(
                artistId,
                contestId,
                1,
                "DELETE",
                entryId
        ));
    }

    private ContestEntrySummaryResponse toEntrySummary(ContestEntry entry) {
        String theme = resolveContestTheme(entry.getContestId());
        LocalDateTime createdAt = entry.getCreatedAt();
        String submittedAt = createdAt != null
                ? createdAt.format(SUBMITTED_FORMATTER)
                : LocalDateTime.now().format(SUBMITTED_FORMATTER);
        return new ContestEntrySummaryResponse(
                entry.getEntryId(),
                entry.getContestId(),
                theme,
                entry.getTitle(),
                entry.getImageUrl(),
                entry.getStatus(),
                submittedAt
        );
    }

    private String resolveContestTheme(Long contestId) {
        try {
            return getContestDetail(contestId).theme();
        } catch (GeneralException ex) {
            if (ex.getErrorCode() == Code.NOT_FOUND) {
                return "Unknown Contest";
            }
            throw ex;
        }
    }

    private ContestSummaryResponse toContestSummary(Contest contest) {
        return new ContestSummaryResponse(
                contest.getContestId(),
                contest.getTheme(),
                contest.getPeriod(),
                contest.getEntryFee(),
                contest.getPrizePool(),
                contest.getDaysLeft(),
                contest.getStatus()
        );
    }

    private Long resolveArtistId(Long userId) {
        return profileArtistRepository.findByUserId(userId)
                .map(ProfileArtist::getArtistId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Profile artist not configured"));
    }
}
