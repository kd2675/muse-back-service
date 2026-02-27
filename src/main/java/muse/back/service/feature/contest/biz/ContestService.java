package muse.back.service.feature.contest.biz;

import lombok.RequiredArgsConstructor;
import muse.back.service.database.pub.dto.AdminContestResponse;
import muse.back.service.database.pub.dto.AdminContestUpsertRequest;
import muse.back.service.database.pub.dto.ContestDetailResponse;
import muse.back.service.database.pub.dto.ContestEntryCreditResponse;
import muse.back.service.database.pub.dto.ContestFinalizeResponse;
import muse.back.service.database.pub.dto.ContestPublicEntryPageResponse;
import muse.back.service.database.pub.dto.ContestPublicEntryResponse;
import muse.back.service.database.pub.dto.ContestRankingResponse;
import muse.back.service.database.pub.dto.ContestEntryResponse;
import muse.back.service.database.pub.dto.ContestEntrySummaryResponse;
import muse.back.service.database.pub.dto.ContestEntrySummaryPageResponse;
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
import muse.back.service.common.util.ImageUrlPathNormalizer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestService {

    private static final DateTimeFormatter SUBMITTED_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final Set<String> ENTRY_VISIBLE_STATUSES = Set.of(STATUS_SUBMITTED, STATUS_APPROVED);
    private static final Set<String> ENTRY_VISIBLE_STATUSES_VOTING = Set.of(STATUS_APPROVED);
    private static final Set<String> ENTRY_VISIBLE_STATUSES_ENDED = Set.of(STATUS_SUBMITTED, STATUS_APPROVED, STATUS_REJECTED);
    private static final String LEDGER_REASON_VOTE = "VOTE";
    private static final String VOTE_REF_PREFIX = "ENTRY:";
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private static final String PHASE_UPCOMING = "UPCOMING";
    private static final String PHASE_SUBMISSION = "SUBMISSION";
    private static final String PHASE_REVIEW = "REVIEW";
    private static final String PHASE_VOTING = "VOTING";
    private static final String PHASE_ENDED = "ENDED";
    private static final Set<String> FINALIZED_STATUSES = Set.of(STATUS_APPROVED, STATUS_REJECTED);
    private static final Set<String> ADMIN_REVIEWABLE_STATUSES = Set.of(STATUS_APPROVED, STATUS_REJECTED);
    private static final long MAX_FILE_SIZE_BYTES = 100L * 1024L * 1024L;
    private static final int MIN_IMAGE_RESOLUTION_PX = 3000;
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png");
    private static final int MAX_PUBLIC_ENTRY_PAGE_SIZE = 50;
    private static final String ENTRY_MODE_RANDOM = "RANDOM";
    private static final String ENTRY_MODE_SUBMITTED_ASC = "SUBMITTED_ASC";

    private final ContestRepository contestRepository;
    private final ContestRuleRepository contestRuleRepository;
    private final ContestEntryRepository contestEntryRepository;
    private final ContestEntryCreditRepository contestEntryCreditRepository;
    private final ContestEntryLedgerRepository contestEntryLedgerRepository;
    private final ProfileArtistRepository profileArtistRepository;
    private final ProfileAwardRepository profileAwardRepository;
    private final ProfileStatRepository profileStatRepository;

    public List<ContestSummaryResponse> getActiveContests() {
        LocalDateTime currentTime = now();
        return contestRepository.findAllByOrderByContestIdAsc()
                .stream()
                .sorted(contestPhaseOrderComparator(currentTime))
                .map(contest -> toContestSummary(contest, currentTime))
                .toList();
    }

    public List<AdminContestResponse> getAdminContests() {
        LocalDateTime currentTime = now();
        return contestRepository.findAllByOrderByContestIdAsc()
                .stream()
                .sorted(contestPhaseOrderComparator(currentTime))
                .map(contest -> toAdminContestResponse(contest, currentTime))
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
                request.submissionStartAt(),
                request.submissionEndAt(),
                request.votingStartAt(),
                request.votingEndAt(),
                0
        );
        contestRepository.save(contest);
        replaceContestRules(contestId, request.rules());
        return toAdminContestResponse(contest, now);
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
                request.submissionStartAt(),
                request.submissionEndAt(),
                request.votingStartAt(),
                request.votingEndAt()
        );
        contestRepository.save(contest);
        replaceContestRules(contestId, request.rules());
        return toAdminContestResponse(contest, now);
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
            return new ContestFinalizeResponse(
                    contestId,
                    PHASE_ENDED,
                    now(),
                    List.of()
            );
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
                entry.updateStatus(STATUS_APPROVED);
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
                entry.updateStatus(STATUS_REJECTED);
            }
        }

        contestEntryRepository.saveAll(sorted);

        return new ContestFinalizeResponse(
                contestId,
                PHASE_ENDED,
                now(),
                winners
        );
    }

    public ContestDetailResponse getContestDetail(Long id) {
        Contest contest = getContestOrThrow(id);
        LocalDateTime currentTime = now();
        String phase = resolveContestPhase(contest, currentTime);
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
                computeDaysLeft(contest.getVotingEndAt(), currentTime),
                phase,
                contest.getSubmissionStartAt(),
                contest.getSubmissionEndAt(),
                contest.getVotingStartAt(),
                contest.getVotingEndAt(),
                resolveParticipationCount(contest.getContestId()),
                rules
        );
    }

    public List<ContestPublicEntryResponse> getContestEntries(Long contestId) {
        Contest contest = getContestOrThrow(contestId);
        String phase = resolveContestPhase(contest, now());
        Set<String> visibleStatuses = resolveVisibleStatusesForPublicPhase(phase);
        List<ContestEntry> entries = contestEntryRepository
                .findByContestIdAndStatusInOrderByCreateDateDesc(contestId, visibleStatuses);
        return toPublicEntryResponses(entries);
    }

    public ContestPublicEntryPageResponse getContestEntriesPage(
            Long contestId,
            String mode,
            Integer page,
            Integer size
    ) {
        Contest contest = getContestOrThrow(contestId);
        String phase = resolveContestPhase(contest, now());
        Set<String> visibleStatuses = resolveVisibleStatusesForPublicPhase(phase);
        String normalizedMode = normalizeEntryMode(mode);
        int resolvedPage = normalizePage(page);
        int resolvedSize = normalizePageSize(size);

        if (ENTRY_MODE_SUBMITTED_ASC.equals(normalizedMode)) {
            PageRequest pageRequest = PageRequest.of(
                    resolvedPage - 1,
                    resolvedSize,
                    Sort.by(Sort.Direction.ASC, "createDate").and(Sort.by(Sort.Direction.ASC, "entryId"))
            );
            Page<ContestEntry> entries = contestEntryRepository
                    .findByContestIdAndStatusIn(contestId, visibleStatuses, pageRequest);
            int totalPages = Math.max(entries.getTotalPages(), 1);
            return new ContestPublicEntryPageResponse(
                    toPublicEntryResponses(entries.getContent()),
                    resolvedPage,
                    resolvedSize,
                    entries.getTotalElements(),
                    totalPages,
                    entries.hasNext(),
                    normalizedMode
            );
        }

        long totalElements = contestEntryRepository.countByContestIdAndStatusIn(contestId, visibleStatuses);
        if (totalElements == 0) {
            return new ContestPublicEntryPageResponse(
                    List.of(),
                    1,
                    resolvedSize,
                    0,
                    1,
                    false,
                    normalizedMode
            );
        }

        List<ContestEntry> randomEntries = contestEntryRepository
                .findRandomByContestIdAndStatusIn(contestId, visibleStatuses, resolvedSize);
        return new ContestPublicEntryPageResponse(
                toPublicEntryResponses(randomEntries),
                1,
                resolvedSize,
                totalElements,
                1,
                false,
                normalizedMode
        );
    }

    @Transactional
    public ContestEntryResponse submitEntry(
            Long contestId,
            Long userId,
            String title,
            String description,
            String fileName,
            String imageUrl,
            Long fileSizeBytes,
            Integer imageWidthPx,
            Integer imageHeightPx
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
        validateUploadedFile(fileName, fileSizeBytes, imageWidthPx, imageHeightPx);
        String normalizedImageUrl = ImageUrlPathNormalizer.toStoragePath(imageUrl);
        String entryId = generateEntryId(contestId);
        consumeEntryCredit(contestId, artistId);

        ContestEntry entry = new ContestEntry(
                entryId,
                artistId,
                contestId,
                title,
                description,
                fileName,
                normalizedImageUrl,
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
        ProfileStat profileStat = findOrCreateProfileStat(artistId);
        profileStat.addWorks(1);
        profileStatRepository.save(profileStat);

        return new ContestEntryResponse(
                contestId,
                entryId,
                title,
                description,
                fileName,
                normalizedImageUrl,
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

        try {
            contestEntryLedgerRepository.save(new ContestEntryLedger(
                    artistId,
                    contestId,
                    0,
                    LEDGER_REASON_VOTE,
                    voteRefId
            ));
        } catch (DataIntegrityViolationException ex) {
            throw new GeneralException(Code.CONFLICT, "Already voted for this entry");
        }

        long selectedEntryVoteCount = countVotesBySelectedEntry(contestId)
                .getOrDefault(entryId, 0L);
        return new ContestVoteResponse(
                contestId,
                entryId,
                selectedEntryVoteCount
        );
    }

    public List<ContestRankingResponse> getContestRanking(Long contestId) {
        Contest contest = getContestOrThrow(contestId);
        String phase = resolveContestPhase(contest, now());
        Set<String> visibleStatuses = resolveVisibleStatusesForPublicPhase(phase);
        List<ContestEntry> entries = contestEntryRepository
                .findByContestIdAndStatusIn(contestId, visibleStatuses);
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

    public List<ContestPublicEntryResponse> getAdminContestEntries(Long contestId) {
        getContestOrThrow(contestId);
        List<ContestEntry> entries = contestEntryRepository.findByContestIdOrderByCreateDateDesc(contestId);
        return toPublicEntryResponses(entries);
    }

    @Transactional
    public ContestPublicEntryResponse updateAdminEntryStatus(Long contestId, String entryId, String status) {
        Contest contest = getContestOrThrow(contestId);
        ensureAdminReviewWindow(contest);
        ContestEntry entry = contestEntryRepository.findByEntryIdAndContestId(entryId, contestId)
                .orElseThrow(() -> new GeneralException(
                        Code.NOT_FOUND,
                        String.format("Entry not found with id: '%s'", entryId)
                ));
        String normalized = normalizeAdminEntryStatus(status);
        entry.updateStatus(normalized);
        contestEntryRepository.save(entry);

        String artistName = profileArtistRepository.findById(entry.getArtistId())
                .map(ProfileArtist::getName)
                .orElse("Unknown Artist");

        return new ContestPublicEntryResponse(
                entry.getEntryId(),
                entry.getContestId(),
                entry.getTitle(),
                entry.getImageUrl(),
                artistName,
                entry.getStatus(),
                formatSubmittedAt(entry)
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
            throw new GeneralException(Code.FORBIDDEN, "Entry credit required for this contest");
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

    public ContestEntrySummaryPageResponse getMyEntriesPage(Long userId, Integer page, Integer size) {
        Long artistId = resolveArtistId(userId);
        int resolvedPage = normalizePage(page);
        int resolvedSize = normalizePageSize(size);
        long totalElements = contestEntryRepository.countByArtistId(artistId);

        if (totalElements == 0) {
            return new ContestEntrySummaryPageResponse(
                    List.of(),
                    1,
                    resolvedSize,
                    0,
                    1,
                    false
            );
        }

        int totalPages = Math.max((int) Math.ceil((double) totalElements / resolvedSize), 1);
        int normalizedPage = Math.min(resolvedPage, totalPages);
        PageRequest pageRequest = PageRequest.of(
                normalizedPage - 1,
                resolvedSize,
                Sort.by(Sort.Direction.DESC, "createDate")
                        .and(Sort.by(Sort.Direction.DESC, "entryId"))
        );
        Page<ContestEntry> pagedEntries = contestEntryRepository.findByArtistId(artistId, pageRequest);

        return new ContestEntrySummaryPageResponse(
                pagedEntries.getContent().stream().map(this::toEntrySummary).toList(),
                normalizedPage,
                resolvedSize,
                totalElements,
                totalPages,
                normalizedPage < totalPages
        );
    }

    @Transactional
    public void deleteEntry(String entryId, Long userId) {
        Long artistId = resolveArtistId(userId);
        ContestEntry entry = contestEntryRepository.findByEntryIdAndArtistId(entryId, artistId)
                .orElseThrow(() -> new GeneralException(Code.NOT_FOUND, String.format("Entry not found with id: '%s'", entryId)));

        // 승인/반려된 출품은 삭제를 제한
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
        ProfileStat profileStat = findOrCreateProfileStat(artistId);
        profileStat.removeWorks(1);
        profileStatRepository.save(profileStat);
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
        if (!ENTRY_VISIBLE_STATUSES_VOTING.contains(entry.getStatus())) {
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

    private String generateEntryId(Long contestId) {
        return "EN-" + contestId + "-" + UUID.randomUUID().toString().replace("-", "");
    }

    private Set<String> resolveVisibleStatusesForPublicPhase(String phase) {
        if (PHASE_VOTING.equals(phase)) {
            return ENTRY_VISIBLE_STATUSES_VOTING;
        }
        if (PHASE_ENDED.equals(phase)) {
            return ENTRY_VISIBLE_STATUSES_ENDED;
        }
        return ENTRY_VISIBLE_STATUSES;
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

    private void validateUploadedFile(
            String fileName,
            Long fileSizeBytes,
            Integer imageWidthPx,
            Integer imageHeightPx
    ) {
        String normalizedName = fileName.trim().toLowerCase();
        boolean validExtension = ALLOWED_IMAGE_EXTENSIONS.stream()
                .anyMatch(normalizedName::endsWith);
        if (!validExtension) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Only JPG/PNG files are allowed");
        }
        if (fileSizeBytes == null || fileSizeBytes <= 0L) {
            throw new GeneralException(Code.VALIDATION_ERROR, "File size is required");
        }
        if (fileSizeBytes > MAX_FILE_SIZE_BYTES) {
            throw new GeneralException(Code.VALIDATION_ERROR, "File size must be 100MB or less");
        }
        if (imageWidthPx == null || imageHeightPx == null) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Image resolution is required");
        }
        if (imageWidthPx < MIN_IMAGE_RESOLUTION_PX || imageHeightPx < MIN_IMAGE_RESOLUTION_PX) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Image resolution must be at least 3000px on both width and height");
        }
    }

    private ProfileStat findOrCreateProfileStat(Long artistId) {
        return profileStatRepository.findByArtistId(artistId)
                .orElseGet(() -> profileStatRepository.save(new ProfileStat(
                        artistId,
                        0,
                        0,
                        0,
                        0
                )));
    }

    private String normalizeAdminEntryStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Status is required");
        }
        String normalized = status.trim().toUpperCase();
        if (!ADMIN_REVIEWABLE_STATUSES.contains(normalized)) {
            throw new GeneralException(Code.VALIDATION_ERROR, "Invalid entry status");
        }
        return normalized;
    }

    private String normalizeEntryMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return ENTRY_MODE_RANDOM;
        }
        String normalized = mode.trim().toUpperCase();
        if (ENTRY_MODE_RANDOM.equals(normalized) || ENTRY_MODE_SUBMITTED_ASC.equals(normalized)) {
            return normalized;
        }
        throw new GeneralException(Code.VALIDATION_ERROR, "Invalid entries mode");
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    private int normalizePageSize(Integer size) {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, MAX_PUBLIC_ENTRY_PAGE_SIZE);
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

    private ContestSummaryResponse toContestSummary(Contest contest, LocalDateTime currentTime) {
        String phase = resolveContestPhase(contest, currentTime);
        return new ContestSummaryResponse(
                contest.getContestId(),
                contest.getTheme(),
                contest.getPeriod(),
                contest.getEntryFee(),
                contest.getPrizePool(),
                computeDaysLeft(contest.getVotingEndAt(), currentTime),
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

    private AdminContestResponse toAdminContestResponse(Contest contest, LocalDateTime currentTime) {
        List<String> rules = contestRuleRepository.findByContestIdOrderBySortOrderAsc(contest.getContestId())
                .stream()
                .map(ContestRule::getRuleText)
                .toList();
        String phase = resolveContestPhase(contest, currentTime);
        return new AdminContestResponse(
                contest.getContestId(),
                contest.getTheme(),
                contest.getDescription(),
                contest.getPeriod(),
                contest.getEntryFee(),
                contest.getPrizePool(),
                computeDaysLeft(contest.getVotingEndAt(), currentTime),
                phase,
                contest.getSubmissionStartAt(),
                contest.getSubmissionEndAt(),
                contest.getVotingStartAt(),
                contest.getVotingEndAt(),
                resolveParticipationCount(contest.getContestId()),
                rules
        );
    }

    private Comparator<Contest> contestPhaseOrderComparator(LocalDateTime currentTime) {
        return Comparator
                .comparingInt((Contest contest) -> resolvePhaseDisplayPriority(resolveContestPhase(contest, currentTime)))
                .thenComparing(Contest::getContestId);
    }

    private int resolvePhaseDisplayPriority(String phase) {
        if (PHASE_VOTING.equals(phase)) {
            return 0;
        }
        if (PHASE_REVIEW.equals(phase)) {
            return 1;
        }
        if (PHASE_SUBMISSION.equals(phase)) {
            return 2;
        }
        if (PHASE_UPCOMING.equals(phase)) {
            return 3;
        }
        return 4;
    }

    private int resolveParticipationCount(Long contestId) {
        return Math.toIntExact(contestEntryRepository.countByContestId(contestId));
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

    private void ensureAdminReviewWindow(Contest contest) {
        LocalDateTime currentTime = now();
        String phase = resolveContestPhase(contest, currentTime);
        if (!PHASE_REVIEW.equals(phase)) {
            throw new GeneralException(
                    Code.CONFLICT,
                    "Entry review is allowed only during review phase"
            );
        }
    }

    private String resolveContestPhase(Contest contest, LocalDateTime currentTime) {
        LocalDateTime submissionStart = contest.getSubmissionStartAt();
        LocalDateTime submissionEnd = contest.getSubmissionEndAt();
        LocalDateTime votingStart = contest.getVotingStartAt();
        LocalDateTime votingEnd = contest.getVotingEndAt();

        if (submissionStart == null || submissionEnd == null || votingStart == null || votingEnd == null) {
            throw new GeneralException(Code.CONFLICT, "Contest schedule is not configured");
        }
        if (currentTime.isBefore(submissionStart)) {
            return PHASE_UPCOMING;
        }
        if (!currentTime.isAfter(submissionEnd)) {
            return PHASE_SUBMISSION;
        }
        if (currentTime.isBefore(votingStart)) {
            return PHASE_REVIEW;
        }
        if (!currentTime.isBefore(votingStart) && !currentTime.isAfter(votingEnd)) {
            return PHASE_VOTING;
        }
        if (currentTime.isAfter(votingEnd)) {
            return PHASE_ENDED;
        }
        return PHASE_UPCOMING;
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
