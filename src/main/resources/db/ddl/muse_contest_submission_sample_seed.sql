-- Add one SUBMISSION sample contest for local/dev testing.
-- Goal: keep at least 2 contests in SUBMISSION phase at current timeline.
-- Safe to re-run.

USE MUSE;

INSERT INTO contest (
    contest_id, theme, description, period, entry_fee, prize_pool, days_left, status,
    submission_start_at, submission_end_at, voting_start_at, voting_end_at,
    participation_count, create_date, update_date
) VALUES (
    107,
    '하이콘트라스트 스터디',
    '출품 진행(SUBMISSION) 상태 테스트용 추가 콘테스트입니다.',
    '2026.02.15 - 2026.03.10',
    5000,
    450000,
    18,
    'ACTIVE',
    '2026-02-15 00:00:00',
    '2026-03-04 23:59:59',
    '2026-03-05 00:00:00',
    '2026-03-10 23:59:59',
    0,
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE
    theme = VALUES(theme),
    description = VALUES(description),
    period = VALUES(period),
    entry_fee = VALUES(entry_fee),
    prize_pool = VALUES(prize_pool),
    days_left = VALUES(days_left),
    status = VALUES(status),
    submission_start_at = VALUES(submission_start_at),
    submission_end_at = VALUES(submission_end_at),
    voting_start_at = VALUES(voting_start_at),
    voting_end_at = VALUES(voting_end_at),
    participation_count = VALUES(participation_count),
    update_date = NOW();

DELETE FROM contest_rule
WHERE contest_id = 107;

INSERT INTO contest_rule (
    contest_id, rule_text, sort_order, create_date, update_date
) VALUES
    (107, '해당 콘테스트 출품권 1개당 1회 출품 가능 (보유 시 횟수 제한 없음, 콘테스트 간 공유 불가)', 1, NOW(), NOW()),
    (107, '최소 3000px 이상의 해상도', 2, NOW(), NOW()),
    (107, '과도한 합성/AI 생성 금지', 3, NOW(), NOW()),
    (107, '투표 기간 동안 출품작별 선택 투표로 진행', 4, NOW(), NOW());
