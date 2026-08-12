-- Muse product expansion: curation, engagement, notifications, drafts, and payments.
-- Apply after 2026-08-12-muse-contest-integrity.sql.
USE MUSE;

ALTER TABLE museum
    ADD COLUMN publish_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' AFTER is_featured,
    ADD COLUMN cover_artwork_id BIGINT NULL AFTER publish_status,
    ADD COLUMN opening_at DATETIME NULL AFTER cover_artwork_id,
    ADD COLUMN curator_note VARCHAR(2000) NULL AFTER opening_at,
    ADD COLUMN layout_preset VARCHAR(30) NOT NULL DEFAULT 'SALON' AFTER curator_note,
    ADD COLUMN lighting_preset VARCHAR(30) NOT NULL DEFAULT 'WARM' AFTER layout_preset;

UPDATE museum
SET publish_status = CASE WHEN is_public = 1 THEN 'PUBLISHED' ELSE 'DRAFT' END;

ALTER TABLE contest_entry
    ADD COLUMN image_url VARCHAR(500) NULL AFTER file_name;

UPDATE contest_entry
SET image_url = CONCAT('/images/', TRIM(LEADING '/' FROM file_name));

ALTER TABLE contest_entry
    MODIFY image_url VARCHAR(500) NOT NULL;

ALTER TABLE museum_artwork
    ADD COLUMN image_url VARCHAR(500) NULL AFTER file_name,
    ADD COLUMN sort_order INT NOT NULL DEFAULT 0 AFTER moderation_status,
    ADD COLUMN room_label VARCHAR(80) NULL AFTER sort_order,
    ADD COLUMN focal_x INT NOT NULL DEFAULT 50 AFTER room_label,
    ADD COLUMN focal_y INT NOT NULL DEFAULT 50 AFTER focal_x,
    ADD COLUMN audio_url VARCHAR(500) NULL AFTER focal_y,
    ADD COLUMN audio_transcript VARCHAR(4000) NULL AFTER audio_url,
    ADD COLUMN lighting_preset VARCHAR(30) NOT NULL DEFAULT 'WARM' AFTER audio_transcript;

UPDATE museum_artwork
SET image_url = CONCAT('/images/', TRIM(LEADING '/' FROM file_name));

ALTER TABLE museum_artwork
    MODIFY image_url VARCHAR(500) NOT NULL;

ALTER TABLE museum
    ADD CONSTRAINT fk_museum_cover_artwork
        FOREIGN KEY (cover_artwork_id) REFERENCES museum_artwork(museum_artwork_id)
        ON DELETE SET NULL;

ALTER TABLE museum_artwork
    DROP FOREIGN KEY fk_museum_artwork_museum;

ALTER TABLE museum_artwork
    ADD CONSTRAINT fk_museum_artwork_museum
        FOREIGN KEY (museum_id) REFERENCES museum(museum_id) ON DELETE CASCADE;

CREATE INDEX idx_museum_artwork_museum_sort
    ON museum_artwork (museum_id, sort_order, museum_artwork_id);

CREATE TABLE muse_notification (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    notification_type VARCHAR(40) NOT NULL,
    title VARCHAR(160) NOT NULL,
    message VARCHAR(500) NOT NULL,
    href VARCHAR(500) NULL,
    dedupe_key VARCHAR(160) NULL,
    read_at DATETIME NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_muse_notification_artist
        FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id),
    CONSTRAINT uk_muse_notification_artist_dedupe
        UNIQUE (artist_id, dedupe_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_muse_notification_artist_read
    ON muse_notification (artist_id, read_at, notification_id);

CREATE TABLE artist_follow (
    artist_follow_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_artist_id BIGINT NOT NULL,
    followed_artist_id BIGINT NOT NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_artist_follow_follower
        FOREIGN KEY (follower_artist_id) REFERENCES profile_artist(artist_id),
    CONSTRAINT fk_artist_follow_followed
        FOREIGN KEY (followed_artist_id) REFERENCES profile_artist(artist_id),
    CONSTRAINT uk_artist_follow_pair UNIQUE (follower_artist_id, followed_artist_id),
    CONSTRAINT chk_artist_follow_self CHECK (follower_artist_id <> followed_artist_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_artist_follow_followed
    ON artist_follow (followed_artist_id, artist_follow_id);

CREATE TABLE museum_bookmark (
    museum_bookmark_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    museum_id BIGINT NOT NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_museum_bookmark_artist
        FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id),
    CONSTRAINT fk_museum_bookmark_museum
        FOREIGN KEY (museum_id) REFERENCES museum(museum_id) ON DELETE CASCADE,
    CONSTRAINT uk_museum_bookmark_pair UNIQUE (artist_id, museum_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_museum_bookmark_artist
    ON museum_bookmark (artist_id, museum_bookmark_id);

CREATE TABLE museum_view_history (
    museum_view_history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    museum_id BIGINT NOT NULL,
    last_artwork_id BIGINT NULL,
    progress_percent INT NOT NULL DEFAULT 0,
    viewed_at DATETIME NOT NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_museum_view_artist
        FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id),
    CONSTRAINT fk_museum_view_museum
        FOREIGN KEY (museum_id) REFERENCES museum(museum_id) ON DELETE CASCADE,
    CONSTRAINT fk_museum_view_artwork
        FOREIGN KEY (last_artwork_id) REFERENCES museum_artwork(museum_artwork_id) ON DELETE SET NULL,
    CONSTRAINT uk_museum_view_pair UNIQUE (artist_id, museum_id),
    CONSTRAINT chk_museum_view_progress CHECK (progress_percent BETWEEN 0 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_museum_view_artist_date
    ON museum_view_history (artist_id, viewed_at, museum_view_history_id);

CREATE TABLE contest_entry_draft (
    contest_entry_draft_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    contest_id BIGINT NOT NULL,
    title VARCHAR(200) NULL,
    description VARCHAR(2000) NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_contest_entry_draft_artist
        FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id),
    CONSTRAINT fk_contest_entry_draft_contest
        FOREIGN KEY (contest_id) REFERENCES contest(contest_id),
    CONSTRAINT uk_contest_entry_draft_pair UNIQUE (artist_id, contest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payment_order (
    payment_order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(64) NOT NULL,
    artist_id BIGINT NOT NULL,
    contest_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL,
    order_name VARCHAR(120) NOT NULL,
    amount INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_key VARCHAR(200) NULL,
    payment_secret VARCHAR(200) NULL,
    idempotency_key VARCHAR(64) NOT NULL,
    receipt_url VARCHAR(500) NULL,
    failure_code VARCHAR(80) NULL,
    failure_message VARCHAR(500) NULL,
    paid_at DATETIME NULL,
    canceled_at DATETIME NULL,
    version BIGINT NOT NULL DEFAULT 0,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_payment_order_artist
        FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id),
    CONSTRAINT fk_payment_order_contest
        FOREIGN KEY (contest_id) REFERENCES contest(contest_id),
    CONSTRAINT uk_payment_order_order_id UNIQUE (order_id),
    CONSTRAINT uk_payment_order_payment_key UNIQUE (payment_key),
    CONSTRAINT chk_payment_order_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_payment_order_artist_date
    ON payment_order (artist_id, payment_order_id);

CREATE INDEX idx_payment_order_status
    ON payment_order (status, payment_order_id);
