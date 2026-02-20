-- Apply when `contest` table already exists.
-- MySQL 8.0+ supports `ADD COLUMN IF NOT EXISTS`.

USE MUSE;

ALTER TABLE contest
    ADD COLUMN submission_start_at DATETIME NULL,
    ADD COLUMN submission_end_at DATETIME NULL,
    ADD COLUMN voting_start_at DATETIME NULL,
    ADD COLUMN voting_end_at DATETIME NULL;
