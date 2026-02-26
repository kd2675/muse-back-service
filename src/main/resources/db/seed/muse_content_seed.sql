-- Seed data for muse content (home/contest/museum/artwork/profile)
-- Generated: 2026-02-04

use MUSE;

INSERT INTO artwork (
    artwork_id, title, artist, category_key, category_label, description, camera, lens,
    focal_length, aperture, shutter_speed, iso, color_from, color_to, create_date, update_date
) VALUES
    (1, 'Glass River', 'Hanna Lee', 'urban', 'Urban', NULL, 'Leica Q3 · 28mm', NULL, NULL, NULL, NULL, NULL, '#3C2C2C', '#D9B08C', NOW(), NOW()),
    (2, 'Echoes of Fog', 'Minho Park', 'nature', 'Nature', NULL, 'Canon R5 · 70mm', NULL, NULL, NULL, NULL, NULL, '#1F2A44', '#6AA1B8', NOW(), NOW()),
    (3, 'Velvet Night', 'Sora Kim', 'night', 'Night', NULL, 'Sony A7 IV · 50mm', NULL, NULL, NULL, NULL, NULL, '#1C1B1F', '#8C6FF0', NOW(), NOW()),
    (4, 'Bloomline', 'Yuna Cho', 'macro', 'Macro', NULL, 'Fujifilm X-T5 · 80mm', NULL, NULL, NULL, NULL, NULL, '#2F3A2F', '#F1C6B3', NOW(), NOW()),

    (201, 'Stillness of Air', 'Jiyoon Park', 'fineart', 'Fine Art',
     '차분한 빛과 질감을 통해 공기의 움직임을 시각화한 작품.',
     'Sony A7R V', 'FE 50mm F1.2 GM', '50mm', 'f/2.0', '1/160s', 'ISO 200',
     '#1B1B1B', '#C7B89A', NOW(), NOW()),
    (202, 'Golden Horizon', 'Noah Kim', 'landscape', 'Landscape',
     '일출 직전의 황금빛 지평선을 포착한 장면.',
     'Canon EOS R5', 'RF 24-70mm F2.8', '35mm', 'f/5.6', '1/250s', 'ISO 100',
     '#4B3B2F', '#E2C08D', NOW(), NOW()),
    (203, 'City Pulse', 'Arin Lee', 'urban', 'Urban',
     '도시의 리듬과 흐름을 추상적 실루엣으로 담아낸 작품.',
     'Nikon Z8', 'Z 24-120mm F4', '70mm', 'f/4.5', '1/80s', 'ISO 400',
     '#1E2A35', '#6B7C93', NOW(), NOW());

-- Gallery category artworks (4 per category)
INSERT INTO artwork (
    artwork_id, title, artist, category_key, category_label, description, camera, lens,
    focal_length, aperture, shutter_speed, iso, color_from, color_to, create_date, update_date
) VALUES
    (301, 'Nature Echo', 'Hanna Lee', 'nature', 'Nature', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#3C2C2C', '#D9B08C', NOW(), NOW()),
    (302, 'Nature Layer', 'Minho Park', 'nature', 'Nature', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#1F2A44', '#6AA1B8', NOW(), NOW()),
    (303, 'Nature Silence', 'Sora Kim', 'nature', 'Nature', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#1C1B1F', '#8C6FF0', NOW(), NOW()),
    (304, 'Nature Frame', 'Yuna Cho', 'nature', 'Nature', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#2F3A2F', '#F1C6B3', NOW(), NOW()),

    (311, 'Urban Echo', 'Hanna Lee', 'urban', 'Urban', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#3C2C2C', '#D9B08C', NOW(), NOW()),
    (312, 'Urban Layer', 'Minho Park', 'urban', 'Urban', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#1F2A44', '#6AA1B8', NOW(), NOW()),
    (313, 'Urban Silence', 'Sora Kim', 'urban', 'Urban', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#1C1B1F', '#8C6FF0', NOW(), NOW()),
    (314, 'Urban Frame', 'Yuna Cho', 'urban', 'Urban', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#2F3A2F', '#F1C6B3', NOW(), NOW()),

    (321, 'People Echo', 'Hanna Lee', 'people', 'People', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#3C2C2C', '#D9B08C', NOW(), NOW()),
    (322, 'People Layer', 'Minho Park', 'people', 'People', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#1F2A44', '#6AA1B8', NOW(), NOW()),
    (323, 'People Silence', 'Sora Kim', 'people', 'People', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#1C1B1F', '#8C6FF0', NOW(), NOW()),
    (324, 'People Frame', 'Yuna Cho', 'people', 'People', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#2F3A2F', '#F1C6B3', NOW(), NOW()),

    (331, 'Abstract Echo', 'Hanna Lee', 'abstract', 'Abstract', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#3C2C2C', '#D9B08C', NOW(), NOW()),
    (332, 'Abstract Layer', 'Minho Park', 'abstract', 'Abstract', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#1F2A44', '#6AA1B8', NOW(), NOW()),
    (333, 'Abstract Silence', 'Sora Kim', 'abstract', 'Abstract', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#1C1B1F', '#8C6FF0', NOW(), NOW()),
    (334, 'Abstract Frame', 'Yuna Cho', 'abstract', 'Abstract', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#2F3A2F', '#F1C6B3', NOW(), NOW()),

    (341, 'Fine Art Echo', 'Hanna Lee', 'fineart', 'Fine Art', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#3C2C2C', '#D9B08C', NOW(), NOW()),
    (342, 'Fine Art Layer', 'Minho Park', 'fineart', 'Fine Art', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#1F2A44', '#6AA1B8', NOW(), NOW()),
    (343, 'Fine Art Silence', 'Sora Kim', 'fineart', 'Fine Art', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#1C1B1F', '#8C6FF0', NOW(), NOW()),
    (344, 'Fine Art Frame', 'Yuna Cho', 'fineart', 'Fine Art', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#2F3A2F', '#F1C6B3', NOW(), NOW()),

    (351, 'Night Echo', 'Hanna Lee', 'night', 'Night', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#3C2C2C', '#D9B08C', NOW(), NOW()),
    (352, 'Night Layer', 'Minho Park', 'night', 'Night', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#1F2A44', '#6AA1B8', NOW(), NOW()),
    (353, 'Night Silence', 'Sora Kim', 'night', 'Night', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#1C1B1F', '#8C6FF0', NOW(), NOW()),
    (354, 'Night Frame', 'Yuna Cho', 'night', 'Night', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '#2F3A2F', '#F1C6B3', NOW(), NOW());

INSERT INTO artwork_asset (
    artwork_id, file_name, image_url, create_date, update_date
) VALUES
    (1, 'glass-river.jpg', 'https://images.unsplash.com/photo-1514565131-fce0801e5785?auto=format&fit=crop&w=1400&q=80', NOW(), NOW()),
    (2, 'echoes-of-fog.jpg', 'https://images.unsplash.com/photo-1480714378408-67cf0d13bc1f?auto=format&fit=crop&w=1400&q=80', NOW(), NOW()),
    (3, 'velvet-night.jpg', 'https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?auto=format&fit=crop&w=1400&q=80', NOW(), NOW()),
    (4, 'bloomline.jpg', 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=1400&q=80', NOW(), NOW()),
    (201, 'stillness-of-air.jpg', 'https://images.unsplash.com/photo-1493244040629-496f6d136cc3?auto=format&fit=crop&w=1400&q=80', NOW(), NOW()),
    (202, 'golden-horizon.jpg', 'https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=1400&q=80', NOW(), NOW()),
    (203, 'city-pulse.jpg', 'https://images.unsplash.com/photo-1449824913935-59a10b8d2000?auto=format&fit=crop&w=1400&q=80', NOW(), NOW()),
    (301, 'nature-echo.jpg', 'https://images.unsplash.com/photo-1472396961693-142e6e269027?auto=format&fit=crop&w=1400&q=80', NOW(), NOW()),
    (311, 'urban-echo.jpg', 'https://images.unsplash.com/photo-1467269204594-9661b134dd2b?auto=format&fit=crop&w=1400&q=80', NOW(), NOW()),
    (321, 'people-echo.jpg', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=1400&q=80', NOW(), NOW()),
    (331, 'abstract-echo.jpg', 'https://images.unsplash.com/photo-1494256997604-768d1f608cac?auto=format&fit=crop&w=1400&q=80', NOW(), NOW()),
    (341, 'fineart-echo.jpg', 'https://images.unsplash.com/photo-1473448912268-2022ce9509d8?auto=format&fit=crop&w=1400&q=80', NOW(), NOW()),
    (351, 'night-echo.jpg', 'https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?auto=format&fit=crop&w=1400&q=80', NOW(), NOW());

INSERT INTO home_hero (
    badge, headline, subheadline, description, create_date, update_date
) VALUES (
    'TODAY''S PICK',
    '경쟁과 감상이 공존하는 디지털 미술관',
    '참여형 사진 콘테스트와 영구 전시를 하나의 경험으로',
    '엄선된 작품만 전시되는 갤러리에서 오늘의 감동을 만나보세요.',
    NOW(),
    NOW()
);

INSERT INTO home_pick (
    artwork_id, sort_order, create_date, update_date
) VALUES
    (1, 1, NOW(), NOW()),
    (2, 2, NOW(), NOW()),
    (3, 3, NOW(), NOW()),
    (4, 4, NOW(), NOW());

INSERT INTO contest (
    contest_id, theme, description, period, entry_fee, prize_pool, days_left,
    submission_start_at, submission_end_at, voting_start_at, voting_end_at,
    participation_count, create_date, update_date
) VALUES
    (101, '빛의 레이어', '도시와 자연의 경계에서 빛이 어떻게 층을 이루는지 기록해보세요.', '2026.02.01 - 2026.02.10', 3000, 420000, 4, '2026-02-01 00:00:00', '2026-02-07 23:59:59', '2026-02-08 00:00:00', '2026-02-10 23:59:59', 128, NOW(), NOW()),
    (102, '도시의 숨', '도시의 온도와 사람들의 숨결을 담아낸 사진을 모집합니다.', '2026.02.01 - 2026.02.18', 3000, 680000, 11, '2026-02-01 00:00:00', '2026-02-14 23:59:59', '2026-02-15 00:00:00', '2026-02-18 23:59:59', 245, NOW(), NOW()),
    (103, '완벽한 정적', '정적인 순간의 균형과 질감을 포착한 작품을 기다립니다.', '2026.02.01 - 2026.03.05', 3000, 1250000, 25, '2026-02-01 00:00:00', '2026-02-28 23:59:59', '2026-03-01 00:00:00', '2026-03-05 23:59:59', 362, NOW(), NOW()),
    (104, '잔광의 초상', NULL, '2026.01.10 - 2026.01.31', 3000, 980000, 0, '2026-01-10 00:00:00', '2026-01-24 23:59:59', '2026-01-25 00:00:00', '2026-01-31 23:59:59', 0, NOW(), NOW()),
    (105, '도시 야광 기록전', '야간 도시를 주제로 현재 전시 및 투표가 진행 중인 예시 콘테스트입니다.', '2026.02.01 - 2026.12.31', 3000, 540000, 300, '2026-02-01 00:00:00', '2026-02-10 23:59:59', '2026-02-11 00:00:00', '2026-12-31 23:59:59', 3, NOW(), NOW()),
    (106, '미래의 빛 프리뷰', '시작 전(UPCOMING) 상태 예시 콘테스트입니다.', '2026.03.20 - 2026.03.31', 3000, 300000, 47, '2026-03-20 00:00:00', '2026-03-24 23:59:59', '2026-03-25 00:00:00', '2026-03-31 23:59:59', 0, NOW(), NOW()),
    (107, '하이콘트라스트 스터디', '출품 진행(SUBMISSION) 상태 테스트용 추가 콘테스트입니다.', '2026.02.15 - 2026.03.10', 5000, 450000, 18, '2026-02-15 00:00:00', '2026-03-04 23:59:59', '2026-03-05 00:00:00', '2026-03-10 23:59:59', 0, NOW(), NOW());

INSERT INTO contest_rule (
    contest_id, rule_text, sort_order, create_date, update_date
) VALUES
    (101, '해당 콘테스트 출품권 1개당 1회 출품 가능 (보유 시 횟수 제한 없음, 콘테스트 간 공유 불가)', 1, NOW(), NOW()),
    (101, '최소 3000px 이상의 해상도', 2, NOW(), NOW()),
    (101, '과도한 합성/AI 생성 금지', 3, NOW(), NOW()),
    (101, '투표 기간 동안 출품작별 선택 투표로 진행', 4, NOW(), NOW()),

    (102, '해당 콘테스트 출품권 1개당 1회 출품 가능 (보유 시 횟수 제한 없음, 콘테스트 간 공유 불가)', 1, NOW(), NOW()),
    (102, '야간 촬영 시 장노출 허용', 2, NOW(), NOW()),
    (102, '촬영 위치 표기 필수', 3, NOW(), NOW()),
    (102, '투표 기간 동안 출품작별 선택 투표로 진행', 4, NOW(), NOW()),

    (103, '노이즈 보정 최소화', 1, NOW(), NOW()),
    (103, '흑백 사진 허용', 2, NOW(), NOW()),
    (103, '촬영 장비 제한 없음', 3, NOW(), NOW()),
    (103, '투표 기간 동안 출품작별 선택 투표로 진행', 4, NOW(), NOW()),

    (105, '해당 콘테스트 출품권 1개당 1회 출품 가능 (보유 시 횟수 제한 없음, 콘테스트 간 공유 불가)', 1, NOW(), NOW()),
    (105, '도시 야간 촬영/장노출 허용', 2, NOW(), NOW()),
    (105, '저작권 침해 및 과도한 합성 금지', 3, NOW(), NOW()),
    (105, '투표 기간 동안 출품작별 선택 투표로 진행', 4, NOW(), NOW()),

    (106, '해당 콘테스트 출품권 1개당 1회 출품 가능 (보유 시 횟수 제한 없음, 콘테스트 간 공유 불가)', 1, NOW(), NOW()),
    (106, '최소 3000px 이상의 해상도', 2, NOW(), NOW()),
    (106, '과도한 합성/AI 생성 금지', 3, NOW(), NOW()),
    (106, '투표 기간 동안 출품작별 선택 투표로 진행', 4, NOW(), NOW()),

    (107, '해당 콘테스트 출품권 1개당 1회 출품 가능 (보유 시 횟수 제한 없음, 콘테스트 간 공유 불가)', 1, NOW(), NOW()),
    (107, '최소 3000px 이상의 해상도', 2, NOW(), NOW()),
    (107, '과도한 합성/AI 생성 금지', 3, NOW(), NOW()),
    (107, '투표 기간 동안 출품작별 선택 투표로 진행', 4, NOW(), NOW());

INSERT INTO profile_artist (
    artist_id, user_id, name, tagline, profile_color, create_date, update_date
) VALUES
    (501, 3, 'Minji Han', '빛과 질감을 탐구하는 사진가', '#2B2A28', NOW(), NOW()),
    (502, 4, 'Jun Park', '도시 야경과 반사를 담는 포토그래퍼', '#233141', NOW(), NOW()),
    (503, 5, 'Sena Choi', '비 오는 거리의 순간을 기록합니다', '#2D3136', NOW(), NOW());

INSERT INTO museum (
    museum_id, artist_id, name, description, is_public, is_featured, create_date, update_date
) VALUES
    (1, 501, 'Light Archive', '빛과 질감 중심의 큐레이션 뮤지엄', 1, 1, NOW(), NOW()),
    (2, 502, 'Urban Echo Chamber', '도시의 반사와 야경을 모은 전시', 1, 1, NOW(), NOW()),
    (3, 503, 'Rain Memory Rooms', '비와 공기의 레이어를 기록한 작품집', 1, 0, NOW(), NOW());

INSERT INTO museum_artwork (
    museum_artwork_id, museum_id, artist_id, title, description, file_name, image_url, moderation_status, create_date, update_date
) VALUES
    (1, 1, 501, 'Stillness of Air', '차분한 빛과 질감의 순간', 'stillness-of-air.jpg', 'https://images.unsplash.com/photo-1493244040629-496f6d136cc3?auto=format&fit=crop&w=1400&q=80', 'VISIBLE', NOW(), NOW()),
    (2, 1, 501, 'Fine Art Echo', '조형적 리듬을 담은 프레임', 'fineart-echo.jpg', 'https://images.unsplash.com/photo-1473448912268-2022ce9509d8?auto=format&fit=crop&w=1400&q=80', 'VISIBLE', NOW(), NOW()),
    (3, 2, 502, 'City Pulse', '도시의 흐름과 대비', 'city-pulse.jpg', 'https://images.unsplash.com/photo-1449824913935-59a10b8d2000?auto=format&fit=crop&w=1400&q=80', 'VISIBLE', NOW(), NOW()),
    (4, 2, 502, 'Neon Drift', '교차로의 네온 반사', 'neon-drift.jpg', 'https://images.unsplash.com/photo-1514565131-fce0801e5785?auto=format&fit=crop&w=1200&q=80', 'VISIBLE', NOW(), NOW()),
    (5, 3, 503, 'After Rain', '비가 지난 뒤 남은 색감', 'after-rain.jpg', 'https://images.unsplash.com/photo-1480714378408-67cf0d13bc1f?auto=format&fit=crop&w=1200&q=80', 'VISIBLE', NOW(), NOW());

INSERT INTO contest_entry (
    entry_id, artist_id, contest_id, title, description, file_name, image_url, status, create_date, update_date
) VALUES
    ('EN-105-001', 501, 105, 'Neon Drift', '새벽 교차로의 네온 반사', 'neon-drift.jpg', 'https://images.unsplash.com/photo-1514565131-fce0801e5785?auto=format&fit=crop&w=1200&q=80', 'SUBMITTED', NOW(), NOW()),
    ('EN-105-002', 502, 105, 'Silent Crosswalk', '인파가 빠져나간 도심 횡단보도', 'silent-crosswalk.jpg', 'https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?auto=format&fit=crop&w=1200&q=80', 'SUBMITTED', NOW(), NOW()),
    ('EN-105-003', 503, 105, 'After Rain', '비가 그친 직후의 차가운 노면 빛', 'after-rain.jpg', 'https://images.unsplash.com/photo-1480714378408-67cf0d13bc1f?auto=format&fit=crop&w=1200&q=80', 'APPROVED', NOW(), NOW());

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

INSERT INTO profile_stat (
    artist_id, total_works, total_awards, total_earnings, followers, create_date, update_date
) VALUES (
    501, 42, 5, 1530000, 1280, NOW(), NOW()
);

INSERT INTO profile_portfolio (
    portfolio_id, artist_id, title, category, color_from, color_to, create_date, update_date
) VALUES
    (901, 501, 'Silk City', 'Urban', '#1E2A35', '#6B7C93', NOW(), NOW()),
    (902, 501, 'Midnight Bloom', 'Night', '#1B1D2E', '#5A7AA6', NOW(), NOW()),
    (903, 501, 'Quiet Spring', 'Nature', '#4C5B3C', '#C6D19C', NOW(), NOW());

INSERT INTO profile_award (
    award_id, artist_id, contest, rank_label, prize, period, create_date, update_date
) VALUES
    (701, 501, '빛의 레이어', '1st', '500,000원', '2026.01', NOW(), NOW()),
    (702, 501, '도시의 숨', '2nd', '300,000원', '2025.12', NOW(), NOW());
