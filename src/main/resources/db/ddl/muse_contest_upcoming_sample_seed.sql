-- Add one UPCOMING sample contest for state/phase demonstration.
-- Safe to re-run on local/dev.

USE MUSE;

INSERT INTO contest (
    contest_id, theme, description, period, entry_fee, prize_pool, days_left,
    submission_start_at, submission_end_at, voting_start_at, voting_end_at,
    participation_count, create_date, update_date
) VALUES (
    106,
    '미래의 빛 프리뷰',
    '시작 전(UPCOMING) 상태 예시 콘테스트입니다.',
    '2026.03.20 - 2026.03.31',
    3000,
    300000,
    47,
    '2026-03-20 00:00:00',
    '2026-03-24 23:59:59',
    '2026-03-25 00:00:00',
    '2026-03-31 23:59:59',
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
    submission_start_at = VALUES(submission_start_at),
    submission_end_at = VALUES(submission_end_at),
    voting_start_at = VALUES(voting_start_at),
    voting_end_at = VALUES(voting_end_at),
    participation_count = VALUES(participation_count),
    update_date = NOW();

DELETE FROM contest_rule
WHERE contest_id = 106;

INSERT INTO contest_rule (
    contest_id, rule_text, sort_order, create_date, update_date
) VALUES
    (106, '해당 콘테스트 출품권 1개당 1회 출품 가능 (보유 시 횟수 제한 없음, 콘테스트 간 공유 불가)', 1, NOW(), NOW()),
    (106, '최소 3000px 이상의 해상도', 2, NOW(), NOW()),
    (106, '과도한 합성/AI 생성 금지', 3, NOW(), NOW()),
    (106, '투표 기간 동안 출품작별 선택 투표로 진행', 4, NOW(), NOW());
