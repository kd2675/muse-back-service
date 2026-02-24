-- Contest entry credit / entry / ledger tables
-- Generated: 2026-02-04
drop schema MUSE;

create schema MUSE;

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
    image_url VARCHAR(2048) NOT NULL,
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
    image_url VARCHAR(2048) NOT NULL,
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
    contest_id BIGINT PRIMARY KEY,
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
    artist_id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    tagline VARCHAR(255),
    profile_color VARCHAR(20),
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT uk_profile_artist_user UNIQUE (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS museum (
    museum_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(1000),
    is_public TINYINT(1) NOT NULL DEFAULT 1,
    is_featured TINYINT(1) NOT NULL DEFAULT 0,
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
    image_url VARCHAR(2048) NOT NULL,
    moderation_status VARCHAR(20) NOT NULL DEFAULT 'REVIEWING',
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_museum_artwork_museum
        FOREIGN KEY (museum_id) REFERENCES museum(museum_id),
    CONSTRAINT fk_museum_artwork_artist
        FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_museum_artwork_museum
    ON museum_artwork (museum_id);

CREATE INDEX idx_museum_artwork_artist
    ON museum_artwork (artist_id);

CREATE INDEX idx_museum_artwork_status
    ON museum_artwork (moderation_status);

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
    award_id BIGINT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    contest VARCHAR(200) NOT NULL,
    rank_label VARCHAR(20) NOT NULL,
    prize VARCHAR(50) NOT NULL,
    period VARCHAR(20) NOT NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_profile_award_artist
        FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
