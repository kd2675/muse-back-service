-- Muse contest integrity migration
-- Apply once to an existing MUSE schema before deploying the matching application revision.
USE MUSE;

ALTER TABLE contest_rule
    DROP FOREIGN KEY fk_contest_rule_contest;

ALTER TABLE contest
    MODIFY contest_id BIGINT NOT NULL AUTO_INCREMENT;

ALTER TABLE contest_rule
    ADD CONSTRAINT fk_contest_rule_contest
        FOREIGN KEY (contest_id) REFERENCES contest(contest_id);

ALTER TABLE museum DROP FOREIGN KEY fk_museum_artist;
ALTER TABLE museum_artwork DROP FOREIGN KEY fk_museum_artwork_artist;
ALTER TABLE profile_award DROP FOREIGN KEY fk_profile_award_artist;
ALTER TABLE profile_portfolio DROP FOREIGN KEY fk_profile_portfolio_artist;
ALTER TABLE profile_stat DROP FOREIGN KEY fk_profile_stat_artist;

ALTER TABLE profile_artist
    MODIFY artist_id BIGINT NOT NULL AUTO_INCREMENT;

ALTER TABLE museum
    ADD CONSTRAINT fk_museum_artist
        FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id);
ALTER TABLE museum_artwork
    ADD CONSTRAINT fk_museum_artwork_artist
        FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id);
ALTER TABLE profile_award
    ADD CONSTRAINT fk_profile_award_artist
        FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id);
ALTER TABLE profile_portfolio
    ADD CONSTRAINT fk_profile_portfolio_artist
        FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id);
ALTER TABLE profile_stat
    ADD CONSTRAINT fk_profile_stat_artist
        FOREIGN KEY (artist_id) REFERENCES profile_artist(artist_id);

ALTER TABLE profile_award
    MODIFY award_id BIGINT NOT NULL AUTO_INCREMENT,
    ADD COLUMN contest_id BIGINT NULL AFTER artist_id,
    ADD COLUMN entry_id VARCHAR(64) NULL AFTER contest_id,
    ADD CONSTRAINT fk_profile_award_contest
        FOREIGN KEY (contest_id) REFERENCES contest(contest_id),
    ADD CONSTRAINT fk_profile_award_entry
        FOREIGN KEY (entry_id) REFERENCES contest_entry(entry_id),
    ADD CONSTRAINT uk_profile_award_contest_entry UNIQUE (contest_id, entry_id);

CREATE TABLE contest_result (
    contest_id BIGINT PRIMARY KEY,
    finalized_at DATETIME NOT NULL,
    finalized_by VARCHAR(64) NOT NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_contest_result_contest
        FOREIGN KEY (contest_id) REFERENCES contest(contest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE image_cleanup_task (
    task_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    reason VARCHAR(40) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    next_attempt_at DATETIME NOT NULL,
    last_error VARCHAR(500),
    create_date DATETIME NOT NULL,
    CONSTRAINT uk_image_cleanup_task_file UNIQUE (file_name),
    INDEX idx_image_cleanup_task_due (next_attempt_at, task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
