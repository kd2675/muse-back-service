-- Add one exhibition/voting-in-progress sample contest with entries.
-- Safe to re-run for local/dev.

USE MUSE;

INSERT INTO contest (
    contest_id, theme, description, period, entry_fee, prize_pool, days_left,
    submission_start_at, submission_end_at, voting_start_at, voting_end_at,
    participation_count, create_date, update_date
) VALUES (
    105,
    '도시 야광 기록전',
    '야간 도시를 주제로 현재 전시 및 투표가 진행 중인 예시 콘테스트입니다.',
    '2026.02.01 - 2026.12.31',
    3000,
    540000,
    300,
    '2026-02-01 00:00:00',
    '2026-02-10 23:59:59',
    '2026-02-11 00:00:00',
    '2026-12-31 23:59:59',
    3,
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

DELETE FROM contest_rule WHERE contest_id = 105;

INSERT INTO contest_rule (
    contest_id, rule_text, sort_order, create_date, update_date
) VALUES
    (105, '해당 콘테스트 출품권 1개당 1회 출품 가능 (보유 시 횟수 제한 없음, 콘테스트 간 공유 불가)', 1, NOW(), NOW()),
    (105, '도시 야간 촬영/장노출 허용', 2, NOW(), NOW()),
    (105, '저작권 침해 및 과도한 합성 금지', 3, NOW(), NOW()),
    (105, '투표 기간 동안 출품작별 선택 투표로 진행', 4, NOW(), NOW());

INSERT INTO profile_artist (
    artist_id, user_id, name, tagline, profile_color, create_date, update_date
) VALUES
    (502, 4, 'Jun Park', '도시 야경과 반사를 담는 포토그래퍼', '#233141', NOW(), NOW()),
    (503, 5, 'Sena Choi', '비 오는 거리의 순간을 기록합니다', '#2D3136', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    tagline = VALUES(tagline),
    profile_color = VALUES(profile_color),
    update_date = NOW();

INSERT INTO contest_entry (
    entry_id, artist_id, contest_id, title, description, file_name, image_url, status, create_date, update_date
) VALUES
    ('EN-105-001', 501, 105, 'Neon Drift', '새벽 교차로의 네온 반사', 'neon-drift.jpg', 'https://images.unsplash.com/photo-1514565131-fce0801e5785?auto=format&fit=crop&w=1200&q=80', 'SUBMITTED', NOW(), NOW()),
    ('EN-105-002', 502, 105, 'Silent Crosswalk', '인파가 빠져나간 도심 횡단보도', 'silent-crosswalk.jpg', 'https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?auto=format&fit=crop&w=1200&q=80', 'SUBMITTED', NOW(), NOW()),
    ('EN-105-003', 503, 105, 'After Rain', '비가 그친 직후의 차가운 노면 빛', 'after-rain.jpg', 'https://images.unsplash.com/photo-1480714378408-67cf0d13bc1f?auto=format&fit=crop&w=1200&q=80', 'APPROVED', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    file_name = VALUES(file_name),
    image_url = VALUES(image_url),
    status = VALUES(status),
    update_date = NOW();

DELETE FROM contest_entry_ledger
WHERE contest_id = 105
  AND reason = 'VOTE';

INSERT INTO contest_entry_ledger (
    artist_id, contest_id, delta, reason, ref_id, create_date, update_date
) VALUES
    (601, 105, 0, 'VOTE', 'ENTRY:EN-105-001', NOW(), NOW()),
    (602, 105, 0, 'VOTE', 'ENTRY:EN-105-001', NOW(), NOW()),
    (603, 105, 0, 'VOTE', 'ENTRY:EN-105-002', NOW(), NOW()),
    (604, 105, 0, 'VOTE', 'ENTRY:EN-105-002', NOW(), NOW()),
    (605, 105, 0, 'VOTE', 'ENTRY:EN-105-002', NOW(), NOW()),
    (606, 105, 0, 'VOTE', 'ENTRY:EN-105-002', NOW(), NOW()),
    (607, 105, 0, 'VOTE', 'ENTRY:EN-105-003', NOW(), NOW()),
    (608, 105, 0, 'VOTE', 'ENTRY:EN-105-003', NOW(), NOW()),
    (609, 105, 0, 'VOTE', 'ENTRY:EN-105-003', NOW(), NOW());
