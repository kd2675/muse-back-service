-- Muse baseline schema. This file is intentionally non-destructive.
-- Existing environments must apply the additive migrations in db/migration instead.
CREATE SCHEMA IF NOT EXISTS MUSE;

use MUSE;

CREATE TABLE IF NOT EXISTS contest_entry_credit (
    contest_entry_credit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    contest_id BIGINT NOT NULL,
    balance INT NOT NULL,
    version BIGINT NOT NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT uk_contest_entry_credit_artist_contest
        UNIQUE (artist_id, contest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_contest_entry_credit_artist
    ON contest_entry_credit (artist_id);

CREATE INDEX idx_contest_entry_credit_contest
    ON contest_entry_credit (contest_id);

CREATE TABLE IF NOT EXISTS contest_entry_ledger (
    contest_entry_ledger_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    contest_id BIGINT NOT NULL,
    delta INT NOT NULL,
    reason VARCHAR(30) NOT NULL,
    ref_id VARCHAR(100) NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT uk_contest_entry_ledger_vote
        UNIQUE (artist_id, contest_id, reason, ref_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_contest_entry_ledger_artist
    ON contest_entry_ledger (artist_id);

CREATE INDEX idx_contest_entry_ledger_contest
    ON contest_entry_ledger (contest_id);

CREATE TABLE IF NOT EXISTS contest_entry (
    entry_id VARCHAR(64) PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    contest_id BIGINT NOT NULL,
    title VARCHAR(200) NULL,
    description VARCHAR(2000) NULL,
    file_name VARCHAR(255) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_contest_entry_artist
    ON contest_entry (artist_id);

CREATE INDEX idx_contest_entry_contest
    ON contest_entry (contest_id);
-- Core content tables for muse (home/contest/museum/artwork/profile)
-- Generated: 2026-02-04

CREATE TABLE IF NOT EXISTS artwork (
    artwork_id BIGINT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    artist VARCHAR(100) NOT NULL,
    category_key VARCHAR(50),
    category_label VARCHAR(50),
    description TEXT,
    camera VARCHAR(200),
    lens VARCHAR(200),
    focal_length VARCHAR(50),
    aperture VARCHAR(50),
    shutter_speed VARCHAR(50),
    iso VARCHAR(50),
    color_from VARCHAR(20),
    color_to VARCHAR(20),
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_artwork_category_key
    ON artwork (category_key);

CREATE TABLE IF NOT EXISTS artwork_asset (
    artwork_id BIGINT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_artwork_asset_artwork
        FOREIGN KEY (artwork_id) REFERENCES artwork(artwork_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS home_hero (
    home_hero_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    badge VARCHAR(60) NOT NULL,
    headline VARCHAR(255) NOT NULL,
    subheadline VARCHAR(255) NOT NULL,
    description VARCHAR(500) NOT NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS home_pick (
    home_pick_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    artwork_id BIGINT NOT NULL,
    sort_order INT NOT NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_home_pick_artwork
        FOREIGN KEY (artwork_id) REFERENCES artwork(artwork_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS contest (
    contest_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    theme VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    period VARCHAR(50) NOT NULL,
    entry_fee INT NOT NULL,
    prize_pool INT NOT NULL,
    days_left INT NOT NULL,
    submission_start_at DATETIME NULL,
    submission_end_at DATETIME NULL,
    voting_start_at DATETIME NULL,
    voting_end_at DATETIME NULL,
    participation_count INT NOT NULL DEFAULT 0,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS contest_rule (
    contest_rule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contest_id BIGINT NOT NULL,
    rule_text VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_contest_rule_contest
        FOREIGN KEY (contest_id) REFERENCES contest(contest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS profile_artist (
    artist_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_key VARCHAR(64) NOT NULL,
    name VARCHAR(100) NOT NULL,
    tagline VARCHAR(255),
    profile_color VARCHAR(20),
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT uk_profile_artist_user UNIQUE (user_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS museum (
    museum_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(1000),
    is_public TINYINT(1) NOT NULL DEFAULT 0,
    is_featured TINYINT(1) NOT NULL DEFAULT 0,
    publish_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    cover_artwork_id BIGINT NULL,
    opening_at DATETIME NULL,
    curator_note VARCHAR(2000),
    layout_preset VARCHAR(30) NOT NULL DEFAULT 'SALON',
    lighting_preset VARCHAR(30) NOT NULL DEFAULT 'WARM',
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_museum_artist
        FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_museum_artist
    ON museum (artist_id);

CREATE INDEX idx_museum_public_featured
    ON museum (is_public, is_featured, museum_id);

CREATE TABLE IF NOT EXISTS museum_artwork (
    museum_artwork_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    museum_id BIGINT NOT NULL,
    artist_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    file_name VARCHAR(255) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    moderation_status VARCHAR(20) NOT NULL DEFAULT 'REVIEWING',
    sort_order INT NOT NULL DEFAULT 0,
    room_label VARCHAR(80),
    focal_x INT NOT NULL DEFAULT 50,
    focal_y INT NOT NULL DEFAULT 50,
    audio_url VARCHAR(500),
    audio_transcript VARCHAR(4000),
    lighting_preset VARCHAR(30) NOT NULL DEFAULT 'WARM',
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_museum_artwork_museum
        FOREIGN KEY (museum_id) REFERENCES museum(museum_id) ON DELETE CASCADE,
    CONSTRAINT fk_museum_artwork_artist
        FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_museum_artwork_museum
    ON museum_artwork (museum_id);

CREATE INDEX idx_museum_artwork_artist
    ON museum_artwork (artist_id);

CREATE INDEX idx_museum_artwork_status
    ON museum_artwork (moderation_status);

CREATE INDEX idx_museum_artwork_museum_sort
    ON museum_artwork (museum_id, sort_order, museum_artwork_id);

ALTER TABLE museum
    ADD CONSTRAINT fk_museum_cover_artwork
        FOREIGN KEY (cover_artwork_id) REFERENCES museum_artwork(museum_artwork_id)
        ON DELETE SET NULL;

CREATE TABLE IF NOT EXISTS profile_stat (
    profile_stat_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    total_works INT NOT NULL,
    total_awards INT NOT NULL,
    total_earnings INT NOT NULL,
    followers INT NOT NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_profile_stat_artist
        FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id),
    CONSTRAINT uk_profile_stat_artist UNIQUE (artist_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS image_cleanup_task (
    task_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    reason VARCHAR(40) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    next_attempt_at DATETIME NOT NULL,
    last_error VARCHAR(500),
    create_date DATETIME NOT NULL,
    CONSTRAINT uk_image_cleanup_task_file UNIQUE (file_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_image_cleanup_task_due
    ON image_cleanup_task (next_attempt_at, task_id);

CREATE TABLE IF NOT EXISTS profile_portfolio (
    portfolio_id BIGINT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(50),
    color_from VARCHAR(20),
    color_to VARCHAR(20),
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_profile_portfolio_artist
        FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS profile_award (
    award_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    contest_id BIGINT NULL,
    entry_id VARCHAR(64) NULL,
    contest VARCHAR(200) NOT NULL,
    rank_label VARCHAR(20) NOT NULL,
    prize VARCHAR(50) NOT NULL,
    period VARCHAR(20) NOT NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_profile_award_artist
        FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id),
    CONSTRAINT fk_profile_award_contest
        FOREIGN KEY (contest_id) REFERENCES contest(contest_id),
    CONSTRAINT fk_profile_award_entry
        FOREIGN KEY (entry_id) REFERENCES contest_entry(entry_id),
    CONSTRAINT uk_profile_award_contest_entry UNIQUE (contest_id, entry_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS contest_result (
    contest_id BIGINT PRIMARY KEY,
    finalized_at DATETIME NOT NULL,
    finalized_by VARCHAR(64) NOT NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_contest_result_contest
        FOREIGN KEY (contest_id) REFERENCES contest(contest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS muse_notification (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    notification_type VARCHAR(40) NOT NULL,
    title VARCHAR(160) NOT NULL,
    message VARCHAR(500) NOT NULL,
    href VARCHAR(500),
    dedupe_key VARCHAR(160),
    read_at DATETIME,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_muse_notification_artist FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id),
    CONSTRAINT uk_muse_notification_artist_dedupe UNIQUE (artist_id, dedupe_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_muse_notification_artist_read
    ON muse_notification (artist_id, read_at, notification_id);

CREATE TABLE IF NOT EXISTS artist_follow (
    artist_follow_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_artist_id BIGINT NOT NULL,
    followed_artist_id BIGINT NOT NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_artist_follow_follower FOREIGN KEY (follower_artist_id) REFERENCES profile_artist(artist_id),
    CONSTRAINT fk_artist_follow_followed FOREIGN KEY (followed_artist_id) REFERENCES profile_artist(artist_id),
    CONSTRAINT uk_artist_follow_pair UNIQUE (follower_artist_id, followed_artist_id),
    CONSTRAINT chk_artist_follow_self CHECK (follower_artist_id <> followed_artist_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_artist_follow_followed ON artist_follow (followed_artist_id, artist_follow_id);

CREATE TABLE IF NOT EXISTS museum_bookmark (
    museum_bookmark_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    museum_id BIGINT NOT NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_museum_bookmark_artist FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id),
    CONSTRAINT fk_museum_bookmark_museum FOREIGN KEY (museum_id) REFERENCES museum(museum_id) ON DELETE CASCADE,
    CONSTRAINT uk_museum_bookmark_pair UNIQUE (artist_id, museum_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_museum_bookmark_artist ON museum_bookmark (artist_id, museum_bookmark_id);

CREATE TABLE IF NOT EXISTS museum_view_history (
    museum_view_history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    museum_id BIGINT NOT NULL,
    last_artwork_id BIGINT NULL,
    progress_percent INT NOT NULL DEFAULT 0,
    viewed_at DATETIME NOT NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_museum_view_artist FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id),
    CONSTRAINT fk_museum_view_museum FOREIGN KEY (museum_id) REFERENCES museum(museum_id) ON DELETE CASCADE,
    CONSTRAINT fk_museum_view_artwork FOREIGN KEY (last_artwork_id) REFERENCES museum_artwork(museum_artwork_id) ON DELETE SET NULL,
    CONSTRAINT uk_museum_view_pair UNIQUE (artist_id, museum_id),
    CONSTRAINT chk_museum_view_progress CHECK (progress_percent BETWEEN 0 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_museum_view_artist_date ON museum_view_history (artist_id, viewed_at, museum_view_history_id);

CREATE TABLE IF NOT EXISTS contest_entry_draft (
    contest_entry_draft_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    contest_id BIGINT NOT NULL,
    title VARCHAR(200),
    description VARCHAR(2000),
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_contest_entry_draft_artist FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id),
    CONSTRAINT fk_contest_entry_draft_contest FOREIGN KEY (contest_id) REFERENCES contest(contest_id),
    CONSTRAINT uk_contest_entry_draft_pair UNIQUE (artist_id, contest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payment_order (
    payment_order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(64) NOT NULL,
    artist_id BIGINT NOT NULL,
    contest_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL,
    order_name VARCHAR(120) NOT NULL,
    amount INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_key VARCHAR(200),
    payment_secret VARCHAR(200),
    idempotency_key VARCHAR(64) NOT NULL,
    receipt_url VARCHAR(500),
    failure_code VARCHAR(80),
    failure_message VARCHAR(500),
    paid_at DATETIME,
    canceled_at DATETIME,
    version BIGINT NOT NULL DEFAULT 0,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_payment_order_artist FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id),
    CONSTRAINT fk_payment_order_contest FOREIGN KEY (contest_id) REFERENCES contest(contest_id),
    CONSTRAINT uk_payment_order_order_id UNIQUE (order_id),
    CONSTRAINT uk_payment_order_payment_key UNIQUE (payment_key),
    CONSTRAINT chk_payment_order_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_payment_order_artist_date ON payment_order (artist_id, payment_order_id);
CREATE INDEX idx_payment_order_status ON payment_order (status, payment_order_id);
