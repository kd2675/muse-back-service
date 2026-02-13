-- Seed data for muse content (home/contest/gallery/artwork/profile)
-- Generated: 2026-02-04

use MUSE;

INSERT INTO gallery_category (
    category_key, title, description, item_count, color_from, color_to, create_date, update_date
) VALUES
    ('nature', 'Nature', '고요한 자연의 리듬', 312, '#4C5B3C', '#C6D19C', NOW(), NOW()),
    ('urban', 'Urban', '도시의 질감과 빛', 245, '#2E2E38', '#BFA7A0', NOW(), NOW()),
    ('people', 'People', '인물의 서사', 198, '#3A2E2A', '#E3B587', NOW(), NOW()),
    ('abstract', 'Abstract', '형태의 실험', 154, '#2B3A4A', '#C7A7E5', NOW(), NOW()),
    ('fineart', 'Fine Art', '작품성 중심', 221, '#2E2A25', '#D7C7A8', NOW(), NOW()),
    ('night', 'Night', '밤의 색감', 176, '#1B1D2E', '#5A7AA6', NOW(), NOW()),
    ('macro', 'Macro', '미세한 디테일에 집중', 12, '#2F3A2F', '#F1C6B3', NOW(), NOW()),
    ('landscape', 'Landscape', '광활한 풍경의 깊이', 18, '#4B3B2F', '#E2C08D', NOW(), NOW());

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

INSERT INTO gallery_highlight (
    artwork_id, sort_order, create_date, update_date
) VALUES
    (201, 1, NOW(), NOW()),
    (202, 2, NOW(), NOW()),
    (203, 3, NOW(), NOW());

INSERT INTO contest (
    contest_id, theme, description, period, entry_fee, prize_pool, days_left, status, participation_count, create_date, update_date
) VALUES
    (101, '빛의 레이어', '도시와 자연의 경계에서 빛이 어떻게 층을 이루는지 기록해보세요.', '2026.02.01 - 2026.02.07', 3000, 420000, 4, 'ACTIVE', 128, NOW(), NOW()),
    (102, '도시의 숨', '도시의 온도와 사람들의 숨결을 담아낸 사진을 모집합니다.', '2026.02.01 - 2026.02.14', 3000, 680000, 11, 'ACTIVE', 245, NOW(), NOW()),
    (103, '완벽한 정적', '정적인 순간의 균형과 질감을 포착한 작품을 기다립니다.', '2026.02.01 - 2026.02.28', 3000, 1250000, 25, 'ACTIVE', 362, NOW(), NOW()),
    (104, '잔광의 초상', NULL, '2026.01.10 - 2026.01.31', 3000, 980000, 0, 'ENDED', 0, NOW(), NOW());

INSERT INTO contest_rule (
    contest_id, rule_text, sort_order, create_date, update_date
) VALUES
    (101, '1인 1작품만 제출 가능', 1, NOW(), NOW()),
    (101, '최소 3000px 이상의 해상도', 2, NOW(), NOW()),
    (101, '과도한 합성/AI 생성 금지', 3, NOW(), NOW()),
    (101, '투표는 A/B 방식으로 진행', 4, NOW(), NOW()),

    (102, '1인 2작품까지 제출 가능', 1, NOW(), NOW()),
    (102, '야간 촬영 시 장노출 허용', 2, NOW(), NOW()),
    (102, '촬영 위치 표기 필수', 3, NOW(), NOW()),
    (102, '투표는 A/B 방식으로 진행', 4, NOW(), NOW()),

    (103, '노이즈 보정 최소화', 1, NOW(), NOW()),
    (103, '흑백 사진 허용', 2, NOW(), NOW()),
    (103, '촬영 장비 제한 없음', 3, NOW(), NOW()),
    (103, '투표는 A/B 방식으로 진행', 4, NOW(), NOW());

INSERT INTO profile_artist (
    artist_id, user_id, name, tagline, profile_color, create_date, update_date
) VALUES (
    501, 3, 'Minji Han', '빛과 질감을 탐구하는 사진가', '#2B2A28', NOW(), NOW()
);

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
