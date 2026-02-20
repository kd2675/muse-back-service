package muse.back.service.feature.contest.biz;

import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.AdminContestResponse;
import muse.back.service.database.pub.dto.AdminContestUpsertRequest;
import muse.back.service.database.pub.dto.ContestDetailResponse;
import muse.back.service.database.pub.dto.ContestEntryCreditResponse;
import muse.back.service.database.pub.dto.ContestFinalizeResponse;
import muse.back.service.database.pub.dto.ContestPublicEntryResponse;
import muse.back.service.database.pub.dto.ContestRankingResponse;
import muse.back.service.database.pub.dto.ContestEntryResponse;
import muse.back.service.database.pub.dto.ContestEntrySummaryResponse;
import muse.back.service.database.pub.dto.ContestSummaryResponse;
import muse.back.service.database.pub.dto.ContestVoteResponse;
import muse.back.service.database.pub.entity.ContestEntry;
import muse.back.service.database.pub.entity.ContestEntryCredit;
import muse.back.service.database.pub.entity.ContestEntryLedger;
import muse.back.service.database.pub.entity.Contest;
import muse.back.service.database.pub.entity.ContestRule;
import muse.back.service.database.pub.entity.ProfileArtist;
import muse.back.service.database.pub.entity.ProfileAward;
import muse.back.service.database.pub.entity.ProfileStat;
import muse.back.service.database.pub.repository.ContestEntryCreditRepository;
import muse.back.service.database.pub.repository.ContestEntryLedgerRepository;
import muse.back.service.database.pub.repository.ContestEntryRepository;
import muse.back.service.database.pub.repository.ContestRepository;
import muse.back.service.database.pub.repository.ContestRuleRepository;
import muse.back.service.database.pub.repository.ProfileArtistRepository;
import muse.back.service.database.pub.repository.ProfileAwardRepository;
import muse.back.service.database.pub.repository.ProfileStatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.common.core.response.base.exception.GeneralException;
import web.common.core.response.base.vo.Code;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestService {

    private static final DateTimeFormatter SUBMITTED_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final Set<String> ENTRY_VISIBLE_STATUSES = Set.of("SUBMITTED", "REVIEWING", "APPROVED");
    private static final String LEDGER_REASON_VOTE = "VOTE";
    private static final String VOTE_REF_PREFIX = "ENTRY:";
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private static final String PHASE_UPCOMING = "UPCOMING";
    private static final String PHASE_SUBMISSION = "SUBMISSION";
    private static final String PHASE_VOTING = "VOTING";
    private static final String PHASE_ENDED = "ENDED";
    private static final Set<String> FINALIZED_STATUSES = Set.of("APPROVED", "REJECTED");
    private static final String DEFAULT_STATUS_ACTIVE = "ACTIVE";
    private static final Set<String> LIST_VISIBLE_STATUSES = Set.of("ACTIVE", "UPCOMING");

    private final ContestRepository contestRepository;
    private final ContestRuleRepository contestRuleRepository;
    private final ContestEntryRepository contestEntryRepository;
    private final ContestEntryCreditRepository contestEntryCreditRepository;
    private final ContestEntryLedgerRepository contestEntryLedgerRepository;
    private final ProfileArtistRepository profileArtistRepository;
    private final ProfileAwardRepository profileAwardRepository;
    private final ProfileStatRepository profileStatRepository;

    public List<ContestSummaryResponse> getActiveContests() {
        return contestRepository.findAllByOrderByContestIdAsc()
                .stream()
                .filter(contest -> LIST_VISIBLE_STATUSES.contains(contest.getStatus()))
                .map(this::toContestSummary)
                .toList();
    }

    public List<AdminContestResponse> getAdminContests() {
        return contestRepository.findAllByOrderByContestIdAsc()
                .stream()
                .map(this::toAdminContestResponse)
                .toList();
    }

    @Transactional
    public AdminContestResponse createContest(AdminContestUpsertRequest request) {
        validateAdminContestRequest(request);
        Long contestId = contestRepository.findTopByOrderByContestIdDesc()
                .map(contest -> contest.getContestId() + 1)
                .orElse(1L);
        LocalDateTime now = now();
        Contest contest = new Contest(
                contestId,
                request.theme().trim(),
                emptyToNull(request.description()),
                buildPeriodText(request.submissionStartAt(), request.votingEndAt()),
                request.entryFee(),
                request.prizePool(),
                computeDaysLeft(request.votingEndAt(), now),
                normalizeContestStatus(request.status()),
                request.submissionStartAt(),
                request.submissionEndAt(),
                request.votingStartAt(),
                request.votingEndAt(),
                0
        );
        contestRepository.save(contest);
        replaceContestRules(contestId, request.rules());
        return toAdminContestResponse(contest);
    }

    @Transactional
    public AdminContestResponse updateContest(Long contestId, AdminContestUpsertRequest request) {
        validateAdminContestRequest(request);
        Contest contest = getContestOrThrow(contestId);
        LocalDateTime now = now();
        contest.updateContestInfo(
                request.theme().trim(),
                emptyToNull(request.description()),
                buildPeriodText(request.submissionStartAt(), request.votingEndAt()),
                request.entryFee(),
                request.prizePool(),
                computeDaysLeft(request.votingEndAt(), now),
                normalizeContestStatus(request.status()),
                request.submissionStartAt(),
                request.submissionEndAt(),
                request.votingStartAt(),
                request.votingEndAt()
        );
        contestRepository.save(contest);
        replaceContestRules(contestId, request.rules());
        return toAdminContestResponse(contest);
    }

    @Transactional
    public ContestFinalizeResponse finalizeContestResults(Long contestId) {
        Contest contest = getContestOrThrow(contestId);
        String phase = resolveContestPhase(contest, now());
        if (!PHASE_ENDED.equals(phase)) {
            throw new GeneralException(Code.CONFLICT, "Voting period has not ended");
        }
        boolean alreadyFinalized = contestEntryRepository.existsByContestIdAndStatusIn(
                contestId,
                FINALIZED_STATUSES
        );
        if (alreadyFinalized) {
            throw new GeneralException(Code.CONFLICT, "Contest results already finalized");
        }

        List<ContestEntry> entries = contestEntryRepository
                .findByContestIdAndStatusIn(contestId, ENTRY_VISIBLE_STATUSES);
        if (entries.isEmpty()) {
            throw new GeneralException(Code.CONFLICT, "No entries to finalize");
        }

        Map<String, Long> voteCounts = countVotesBySelectedEntry(contestId);
        Map<Long, String> artistNamesById = resolveArtistNames(entries);
        List<ContestEntry> sorted = entries.stream()
                .sorted(Comparator
                        .comparingLong((ContestEntry entry) -> voteCounts.getOrDefault(entry.getEntryId(), 0L))
                        .reversed()
                        .thenComparing(ContestEntry::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ContestEntry::getEntryId))
                .toList();

        int winnerCount = Math.min(3, sorted.size());
        List<Integer> prizes = splitPrizePool(contest.getPrizePool(), winnerCount);
        long nextAwardId = profileAwardRepository.findTopByOrderByAwardIdDesc()
                .map(award -> award.getAwardId() + 1L)
                .orElse(1L);
        List<ContestFinalizeResponse.Winner> winners = new ArrayList<>();

        for (int index = 0; index < sorted.size(); index++) {
            ContestEntry entry = sorted.get(index);
            if (index < winnerCount) {
                int rank = index + 1;
                int prize = prizes.get(index);
                entry.updateStatus("APPROVED");
                profileAwardRepository.save(new ProfileAward(
                        nextAwardId++,
                        entry.getArtistId(),
                        contest.getTheme(),
                        toRankLabel(rank),
                        toPrizeText(prize),
                        toAwardPeriod(contest.getVotingEndAt())
                ));
                ProfileStat stat = profileStatRepository.findByArtistId(entry.getArtistId())
                        .orElseGet(() -> profileStatRepository.save(new ProfileStat(
                                entry.getArtistId(),
                                0,
                                0,
                                0,
                                0
                        )));
                stat.addAwards(1);
                stat.addEarnings(prize);
                profileStatRepository.save(stat);
                winners.add(new ContestFinalizeResponse.Winner(
                        rank,
                        entry.getEntryId(),
                        entry.getTitle(),
                        artistNamesById.getOrDefault(entry.getArtistId(), "Unknown Artist"),
                        voteCounts.getOrDefault(entry.getEntryId(), 0L),
                        prize
                ));
            } else {
                entry.updateStatus("REJECTED");
            }
        }

        contestEntryRepository.saveAll(sorted);
        contest.markEnded();
        contestRepository.save(contest);

        return new ContestFinalizeResponse(
                contestId,
                PHASE_ENDED,
                now(),
                winners
        );
    }

    public ContestDetailResponse getContestDetail(Long id) {
        Contest contest = getContestOrThrow(id);
        String phase = resolveContestPhase(contest, now());
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
                computeDaysLeft(contest.getVotingEndAt(), now()),
                contest.getStatus(),
                phase,
                contest.getSubmissionStartAt(),
                contest.getSubmissionEndAt(),
                contest.getVotingStartAt(),
                contest.getVotingEndAt(),
                contest.getParticipationCount(),
                rules
        );
    }

    public List<ContestPublicEntryResponse> getContestEntries(Long contestId) {
        getContestDetail(contestId);
        List<ContestEntry> entries = contestEntryRepository
                .findByContestIdAndStatusInOrderByCreateDateDesc(contestId, ENTRY_VISIBLE_STATUSES);
        return toPublicEntryResponses(entries);
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
        Contest contest = getContestOrThrow(contestId);
        ensureSubmissionPhase(contest);
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
                STATUS_SUBMITTED
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
                STATUS_SUBMITTED
        );
    }

    @Transactional
    public ContestEntryCreditResponse purchaseEntryCredit(Long contestId, Long userId) {
        Long artistId = resolveArtistId(userId);
        Contest contest = getContestOrThrow(contestId);
        ensureSubmissionPhase(contest);
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

    @Transactional
    public ContestVoteResponse voteEntry(
            Long contestId,
            Long userId,
            String entryId
    ) {
        Long artistId = resolveArtistId(userId);
        Contest contest = getContestOrThrow(contestId);
        ensureVotingPhase(contest);

        requireNonBlank(entryId, "entryId is required");
        ContestEntry targetEntry = findVisibleEntryForVote(contestId, entryId);
        if (artistId.equals(targetEntry.getArtistId())) {
            throw new GeneralException(Code.FORBIDDEN, "Cannot vote on your own entry");
        }

        String voteRefId = buildVoteRefId(entryId);
        boolean alreadyVoted = contestEntryLedgerRepository
                .existsByArtistIdAndContestIdAndReasonAndRefId(
                        artistId,
                        contestId,
                        LEDGER_REASON_VOTE,
                        voteRefId
                );
        if (alreadyVoted) {
            throw new GeneralException(Code.CONFLICT, "Already voted for this entry");
        }

        contestEntryLedgerRepository.save(new ContestEntryLedger(
                artistId,
                contestId,
                0,
                LEDGER_REASON_VOTE,
                voteRefId
        ));

        long selectedEntryVoteCount = countVotesBySelectedEntry(contestId)
                .getOrDefault(entryId, 0L);
        return new ContestVoteResponse(
                contestId,
                entryId,
                selectedEntryVoteCount
        );
    }

    public List<ContestRankingResponse> getContestRanking(Long contestId) {
        getContestDetail(contestId);
        List<ContestEntry> entries = contestEntryRepository
                .findByContestIdAndStatusIn(contestId, ENTRY_VISIBLE_STATUSES);
        if (entries.isEmpty()) {
            return List.of();
        }

        Map<Long, String> artistNamesById = resolveArtistNames(entries);
        Map<String, Long> voteCounts = countVotesBySelectedEntry(contestId);

        List<ContestEntry> sorted = entries.stream()
                .sorted(Comparator
                        .comparingLong((ContestEntry entry) -> voteCounts.getOrDefault(entry.getEntryId(), 0L))
                        .reversed()
                        .thenComparing(ContestEntry::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ContestEntry::getEntryId))
                .toList();

        List<ContestRankingResponse> ranking = new ArrayList<>();
        for (int index = 0; index < sorted.size(); index++) {
            ContestEntry entry = sorted.get(index);
            ranking.add(new ContestRankingResponse(
                    index + 1,
                    entry.getEntryId(),
                    entry.getTitle(),
                    entry.getImageUrl(),
                    artistNamesById.getOrDefault(entry.getArtistId(), "Unknown Artist"),
                    voteCounts.getOrDefault(entry.getEntryId(), 0L)
            ));
        }
        return ranking;
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
        if (!STATUS_SUBMITTED.equals(entry.getStatus())) {
            throw new GeneralException(Code.CONFLICT, "Only submitted entries can be deleted");
        }

        Long contestId = entry.getContestId();
        Contest contest = getContestOrThrow(contestId);
        ensureSubmissionPhase(contest);
        contestEntryRepository.delete(entry);

        // 출품 취소 시 참여 카운트를 되돌림
        contest.decreaseParticipationCount();
        contestRepository.save(contest);

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
        return new ContestEntrySummaryResponse(
                entry.getEntryId(),
                entry.getContestId(),
                theme,
                entry.getTitle(),
                entry.getImageUrl(),
                entry.getStatus(),
                formatSubmittedAt(entry)
        );
    }

    private List<ContestPublicEntryResponse> toPublicEntryResponses(List<ContestEntry> entries) {
        Map<Long, String> artistNamesById = resolveArtistNames(entries);
        return entries.stream()
                .map(entry -> new ContestPublicEntryResponse(
                        entry.getEntryId(),
                        entry.getContestId(),
                        entry.getTitle(),
                        entry.getImageUrl(),
                        artistNamesById.getOrDefault(entry.getArtistId(), "Unknown Artist"),
                        entry.getStatus(),
                        formatSubmittedAt(entry)
                ))
                .toList();
    }

    private Map<Long, String> resolveArtistNames(List<ContestEntry> entries) {
        Set<Long> artistIds = entries.stream()
                .map(ContestEntry::getArtistId)
                .collect(java.util.stream.Collectors.toSet());
        if (artistIds.isEmpty()) {
            return Map.of();
        }
        return profileArtistRepository.findByArtistIdIn(artistIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        ProfileArtist::getArtistId,
                        ProfileArtist::getName,
                        (left, right) -> left
                ));
    }

    private ContestEntry findVisibleEntryForVote(Long contestId, String entryId) {
        ContestEntry entry = contestEntryRepository.findByEntryIdAndContestId(entryId, contestId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, String.format("Entry not found with id: '%s'", entryId)));
        if (!ENTRY_VISIBLE_STATUSES.contains(entry.getStatus())) {
            throw new GeneralException(Code.CONFLICT, "Entry is not available for voting");
        }
        return entry;
    }

    private Map<String, Long> countVotesBySelectedEntry(Long contestId) {
        List<ContestEntryLedger> voteLedgers = contestEntryLedgerRepository
                .findByContestIdAndReason(contestId, LEDGER_REASON_VOTE);
        Map<String, Long> voteCounts = new HashMap<>();
        for (ContestEntryLedger ledger : voteLedgers) {
            String selectedEntryId = parseSelectedEntryId(ledger.getRefId());
            if (selectedEntryId == null || selectedEntryId.isBlank()) {
                continue;
            }
            voteCounts.merge(selectedEntryId, 1L, Long::sum);
        }
        return voteCounts;
    }

    private String parseSelectedEntryId(String voteRefId) {
        if (voteRefId == null) {
            return null;
        }
        if (voteRefId.startsWith(VOTE_REF_PREFIX)) {
            return voteRefId.substring(VOTE_REF_PREFIX.length());
        }
        return null;
    }

    private String buildVoteRefId(String entryId) {
        return VOTE_REF_PREFIX + entryId;
    }

    private String formatSubmittedAt(ContestEntry entry) {
        LocalDateTime createdAt = entry.getCreatedAt();
        return createdAt != null
                ? createdAt.format(SUBMITTED_FORMATTER)
                : LocalDateTime.now().format(SUBMITTED_FORMATTER);
    }

    private void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new GeneralException(Code.VALIDATION_ERROR, message);
        }
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
        String phase = resolveContestPhase(contest, now());
        return new ContestSummaryResponse(
                contest.getContestId(),
                contest.getTheme(),
                contest.getPeriod(),
                contest.getEntryFee(),
                contest.getPrizePool(),
                computeDaysLeft(contest.getVotingEndAt(), now()),
                contest.getStatus(),
                phase,
                contest.getSubmissionStartAt(),
                contest.getSubmissionEndAt(),
                contest.getVotingStartAt(),
                contest.getVotingEndAt()
        );
    }

    private List<String> normalizeRules(List<String> rules) {
        if (rules == null || rules.isEmpty()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "At least one rule is required");
        }
        List<String> normalized = rules.stream()
                .filter(rule -> rule != null && !rule.isBlank())
                .map(String::trim)
                .toList();
        if (normalized.isEmpty()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "At least one rule is required");
        }
        return normalized;
    }

    private String normalizeContestStatus(String status) {
        if (status == null || status.isBlank()) {
            return DEFAULT_STATUS_ACTIVE;
        }
        String normalized = status.trim().toUpperCase();
        if (!Set.of("UPCOMING", "ACTIVE", "ENDED").contains(normalized)) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Invalid status");
        }
        return normalized;
    }

    private String buildPeriodText(LocalDateTime submissionStartAt, LocalDateTime votingEndAt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return submissionStartAt.format(formatter) + " - " + votingEndAt.format(formatter);
    }

    private int computeDaysLeft(LocalDateTime votingEndAt, LocalDateTime currentTime) {
        if (votingEndAt == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(currentTime.toLocalDate(), votingEndAt.toLocalDate());
        return (int) Math.max(days, 0);
    }

    private List<Integer> splitPrizePool(int prizePool, int winnerCount) {
        if (winnerCount <= 0 || prizePool <= 0) {
            return List.of();
        }
        if (winnerCount == 1) {
            return List.of(prizePool);
        }
        if (winnerCount == 2) {
            int first = (int) Math.floor(prizePool * 0.7);
            return List.of(first, prizePool - first);
        }
        int first = (int) Math.floor(prizePool * 0.5);
        int second = (int) Math.floor(prizePool * 0.3);
        int third = prizePool - first - second;
        return List.of(first, second, third);
    }

    private String toRankLabel(int rank) {
        if (rank == 1) return "1st";
        if (rank == 2) return "2nd";
        if (rank == 3) return "3rd";
        return rank + "th";
    }

    private String toPrizeText(int prize) {
        return String.format("%,d원", prize);
    }

    private String toAwardPeriod(LocalDateTime votingEndAt) {
        LocalDateTime base = votingEndAt != null ? votingEndAt : now();
        return base.format(DateTimeFormatter.ofPattern("yyyy.MM"));
    }

    private String emptyToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private AdminContestResponse toAdminContestResponse(Contest contest) {
        List<String> rules = contestRuleRepository.findByContestIdOrderBySortOrderAsc(contest.getContestId())
                .stream()
                .map(ContestRule::getRuleText)
                .toList();
        String phase = resolveContestPhase(contest, now());
        return new AdminContestResponse(
                contest.getContestId(),
                contest.getTheme(),
                contest.getDescription(),
                contest.getPeriod(),
                contest.getEntryFee(),
                contest.getPrizePool(),
                computeDaysLeft(contest.getVotingEndAt(), now()),
                contest.getStatus(),
                phase,
                contest.getSubmissionStartAt(),
                contest.getSubmissionEndAt(),
                contest.getVotingStartAt(),
                contest.getVotingEndAt(),
                contest.getParticipationCount(),
                rules
        );
    }

    private void replaceContestRules(Long contestId, List<String> rules) {
        contestRuleRepository.deleteByContestId(contestId);
        List<String> normalizedRules = normalizeRules(rules);
        List<ContestRule> entities = new ArrayList<>();
        for (int index = 0; index < normalizedRules.size(); index++) {
            entities.add(new ContestRule(
                    contestId,
                    normalizedRules.get(index),
                    index + 1
            ));
        }
        contestRuleRepository.saveAll(entities);
    }

    private void validateAdminContestRequest(AdminContestUpsertRequest request) {
        if (request == null) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Request is required");
        }
        if (request.theme() == null || request.theme().isBlank()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Theme is required");
        }
        if (request.entryFee() <= 0) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Entry fee must be greater than 0");
        }
        if (request.prizePool() < 0) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Prize pool cannot be negative");
        }
        if (request.submissionStartAt() == null
                || request.submissionEndAt() == null
                || request.votingStartAt() == null
                || request.votingEndAt() == null) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Contest schedule is required");
        }
        if (!request.submissionStartAt().isBefore(request.submissionEndAt())) {
            throw new GeneralException(Code.VALIDATION_ERROR, "submissionStartAt must be before submissionEndAt");
        }
        if (request.submissionEndAt().isAfter(request.votingStartAt())) {
            throw new GeneralException(Code.VALIDATION_ERROR, "submissionEndAt must be before or equal to votingStartAt");
        }
        if (!request.votingStartAt().isBefore(request.votingEndAt())) {
            throw new GeneralException(Code.VALIDATION_ERROR, "votingStartAt must be before votingEndAt");
        }
    }

    private Contest getContestOrThrow(Long contestId) {
        return contestRepository.findById(contestId)
                .orElseThrow(() -> new GeneralException(
                        Code.NOT_FOUND,
                        String.format("Contest not found with id: '%s'", contestId)
                ));
    }

    private void ensureSubmissionPhase(Contest contest) {
        String phase = resolveContestPhase(contest, now());
        if (!PHASE_SUBMISSION.equals(phase)) {
            throw new GeneralException(Code.CONFLICT, "Submission period is closed");
        }
    }

    private void ensureVotingPhase(Contest contest) {
        String phase = resolveContestPhase(contest, now());
        if (!PHASE_VOTING.equals(phase)) {
            throw new GeneralException(Code.CONFLICT, "Voting period is closed");
        }
    }

    private String resolveContestPhase(Contest contest, LocalDateTime currentTime) {
        LocalDateTime submissionStart = contest.getSubmissionStartAt();
        LocalDateTime submissionEnd = contest.getSubmissionEndAt();
        LocalDateTime votingStart = contest.getVotingStartAt();
        LocalDateTime votingEnd = contest.getVotingEndAt();

        if (submissionStart == null || submissionEnd == null || votingStart == null || votingEnd == null) {
            return fallbackPhaseByStatus(contest.getStatus());
        }
        if (currentTime.isBefore(submissionStart)) {
            return PHASE_UPCOMING;
        }
        if (!currentTime.isAfter(submissionEnd)) {
            return PHASE_SUBMISSION;
        }
        if (!currentTime.isBefore(votingStart) && !currentTime.isAfter(votingEnd)) {
            return PHASE_VOTING;
        }
        if (currentTime.isAfter(votingEnd)) {
            return PHASE_ENDED;
        }
        return PHASE_UPCOMING;
    }

    private String fallbackPhaseByStatus(String status) {
        if ("UPCOMING".equals(status)) {
            return PHASE_UPCOMING;
        }
        if ("ENDED".equals(status)) {
            return PHASE_ENDED;
        }
        return PHASE_SUBMISSION;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(SERVICE_ZONE);
    }

    private Long resolveArtistId(Long userId) {
        return profileArtistRepository.findByUserId(userId)
                .map(ProfileArtist::getArtistId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, "Profile artist not configured"));
    }
}
