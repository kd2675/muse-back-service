package muse.back.service.feature.contest.biz;

import muse.back.service.common.exception.MuseException;
import muse.back.service.database.pub.dto.ContestDetailResponse;
import muse.back.service.database.pub.dto.ContestEntryCreditResponse;
import muse.back.service.database.pub.dto.ContestEntryResponse;
import muse.back.service.database.pub.dto.ContestEntrySummaryResponse;
import muse.back.service.database.pub.dto.ContestSummaryResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ContestService {

    private static final DateTimeFormatter SUBMITTED_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final List<ContestEntrySummaryResponse> entryStore =
            new CopyOnWriteArrayList<>(List.of(
                    new ContestEntrySummaryResponse(
                            "EN-101-1700001",
                            101L,
                            "빛의 레이어",
                            "Layered Dawn",
                            null,
                            "SUBMITTED",
                            "2026-02-02 10:45"
                    ),
                    new ContestEntrySummaryResponse(
                            "EN-102-1700002",
                            102L,
                            "도시의 숨",
                            "Urban Breath",
                            null,
                            "SUBMITTED",
                            "2026-02-01 22:10"
                    )
            ));
    private final ConcurrentHashMap<Long, Integer> entryCredits = new ConcurrentHashMap<>();

    public List<ContestSummaryResponse> getActiveContests() {
        return List.of(
                new ContestSummaryResponse(
                        101L,
                        "빛의 레이어",
                        "2026.02.01 - 2026.02.07",
                        3000,
                        420000,
                        4,
                        "ACTIVE"
                ),
                new ContestSummaryResponse(
                        102L,
                        "도시의 숨",
                        "2026.02.01 - 2026.02.14",
                        3000,
                        680000,
                        11,
                        "ACTIVE"
                ),
                new ContestSummaryResponse(
                        103L,
                        "완벽한 정적",
                        "2026.02.01 - 2026.02.28",
                        3000,
                        1250000,
                        25,
                        "ACTIVE"
                ),
                new ContestSummaryResponse(
                        104L,
                        "잔광의 초상",
                        "2026.01.10 - 2026.01.31",
                        3000,
                        980000,
                        0,
                        "ENDED"
                )
        );
    }

    public ContestDetailResponse getContestDetail(Long id) {
        Map<Long, ContestDetailResponse> detailMap = Map.of(
                101L,
                new ContestDetailResponse(
                        101L,
                        "빛의 레이어",
                        "도시와 자연의 경계에서 빛이 어떻게 층을 이루는지 기록해보세요.",
                        "2026.02.01 - 2026.02.07",
                        3000,
                        420000,
                        4,
                        "ACTIVE",
                        128,
                        List.of(
                                "1인 1작품만 제출 가능",
                                "최소 3000px 이상의 해상도",
                                "과도한 합성/AI 생성 금지",
                                "투표는 A/B 방식으로 진행"
                        )
                ),
                102L,
                new ContestDetailResponse(
                        102L,
                        "도시의 숨",
                        "도시의 온도와 사람들의 숨결을 담아낸 사진을 모집합니다.",
                        "2026.02.01 - 2026.02.14",
                        3000,
                        680000,
                        11,
                        "ACTIVE",
                        245,
                        List.of(
                                "1인 2작품까지 제출 가능",
                                "야간 촬영 시 장노출 허용",
                                "촬영 위치 표기 필수",
                                "투표는 A/B 방식으로 진행"
                        )
                ),
                103L,
                new ContestDetailResponse(
                        103L,
                        "완벽한 정적",
                        "정적인 순간의 균형과 질감을 포착한 작품을 기다립니다.",
                        "2026.02.01 - 2026.02.28",
                        3000,
                        1250000,
                        25,
                        "ACTIVE",
                        362,
                        List.of(
                                "노이즈 보정 최소화",
                                "흑백 사진 허용",
                                "촬영 장비 제한 없음",
                                "투표는 A/B 방식으로 진행"
                        )
                )
        );

        ContestDetailResponse detail = detailMap.get(id);
        if (detail == null) {
            throw new MuseException.ResourceNotFoundException("Contest", "id", id);
        }
        return detail;
    }

    public ContestEntryResponse submitEntry(
            Long contestId,
            String title,
            String description,
            String fileName,
            String imageUrl
    ) {
        ContestDetailResponse detail = getContestDetail(contestId);
        if (!"ACTIVE".equals(detail.status())) {
            throw new MuseException.ConflictException("Contest is not active");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new MuseException.ValidationException("Image URL is required");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new MuseException.ValidationException("File name is required");
        }
        consumeEntryCredit(contestId);
        String entryId = "EN-" + contestId + "-" + System.currentTimeMillis();
        String submittedAt = LocalDateTime.now().format(SUBMITTED_FORMATTER);

        entryStore.add(0, new ContestEntrySummaryResponse(
                entryId,
                contestId,
                detail.theme(),
                title,
                imageUrl,
                "SUBMITTED",
                submittedAt
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

    public ContestEntryCreditResponse purchaseEntryCredit(Long contestId) {
        ContestDetailResponse detail = getContestDetail(contestId);
        if (!"ACTIVE".equals(detail.status())) {
            throw new MuseException.ConflictException("Contest is not active");
        }
        int credits = addEntryCredit(contestId);
        return new ContestEntryCreditResponse(contestId, credits, "AVAILABLE");
    }

    public ContestEntryCreditResponse getEntryCreditStatus(Long contestId) {
        getContestDetail(contestId);
        int credits = entryCredits.getOrDefault(contestId, 0);
        return new ContestEntryCreditResponse(
                contestId,
                credits,
                credits > 0 ? "AVAILABLE" : "NONE"
        );
    }

    private int addEntryCredit(Long contestId) {
        return entryCredits.merge(contestId, 1, Integer::sum);
    }

    private void consumeEntryCredit(Long contestId) {
        Integer current = entryCredits.get(contestId);
        if (current == null || current <= 0) {
            throw new MuseException.ForbiddenException("Entry credit required");
        }
        entryCredits.put(contestId, current - 1);
    }

    public List<ContestEntrySummaryResponse> getMyEntries() {
        return new ArrayList<>(entryStore);
    }

    public void deleteEntry(String entryId) {
        boolean removed = entryStore.removeIf(entry -> entry.entryId().equals(entryId));
        if (!removed) {
            throw new MuseException.ResourceNotFoundException("Entry", "id", entryId);
        }
    }
}
