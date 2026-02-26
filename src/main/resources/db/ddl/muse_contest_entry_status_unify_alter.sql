-- Unify contest_entry.status into 3 states only:
-- SUBMITTED (대기), APPROVED (승인), REJECTED (반려)
--
-- Target DB: MySQL 8+
-- Recommended: run on maintenance window / take backup before execution.

-- 1) Normalize legacy REVIEWING -> SUBMITTED-- Unify contest_entry.status into 3 states only:
-- SUBMITTED (대기), APPROVED (승인), REJECTED (반려)
--
-- Target DB: MySQL 8+
-- Recommended: run on maintenance window / take backup before execution.

    use MUSE;

-- 1) Normalize legacy REVIEWING -> SUBMITTED
UPDATE contest_entry
SET status = 'SUBMITTED'
WHERE status = 'REVIEWING';

-- 2) Normalize unexpected values -> SUBMITTED (safe fallback)
UPDATE contest_entry
SET status = 'SUBMITTED'
WHERE status IS NULL
   OR status NOT IN ('SUBMITTED', 'APPROVED', 'REJECTED');

-- 3) Enforce allowed values at schema level
ALTER TABLE contest_entry
    MODIFY COLUMN status ENUM('SUBMITTED', 'APPROVED', 'REJECTED') NOT NULL;

-- 4) Optional performance index for status-based contest queries
SET @has_idx_contest_status := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'contest_entry'
      AND index_name = 'idx_contest_entry_contest_status'
);

SET @idx_sql := IF(
        @has_idx_contest_status = 0,
        'CREATE INDEX idx_contest_entry_contest_status ON contest_entry (contest_id, status)',
        'SELECT ''idx_contest_entry_contest_status already exists'' AS message'
                );

PREPARE stmt_idx FROM @idx_sql;
EXECUTE stmt_idx;
DEALLOCATE PREPARE stmt_idx;

UPDATE contest_entry
SET status = 'SUBMITTED'
WHERE status = 'REVIEWING';

-- 2) Normalize unexpected values -> SUBMITTED (safe fallback)
UPDATE contest_entry
SET status = 'SUBMITTED'
WHERE status IS NULL
   OR status NOT IN ('SUBMITTED', 'APPROVED', 'REJECTED');

-- 3) Enforce allowed values at schema level
ALTER TABLE contest_entry
    MODIFY COLUMN status ENUM('SUBMITTED', 'APPROVED', 'REJECTED') NOT NULL;

-- 4) Optional performance index for status-based contest queries
SET @has_idx_contest_status := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'contest_entry'
      AND index_name = 'idx_contest_entry_contest_status'
);

SET @idx_sql := IF(
    @has_idx_contest_status = 0,
    'CREATE INDEX idx_contest_entry_contest_status ON contest_entry (contest_id, status)',
    'SELECT ''idx_contest_entry_contest_status already exists'' AS message'
);

PREPARE stmt_idx FROM @idx_sql;
EXECUTE stmt_idx;
DEALLOCATE PREPARE stmt_idx;
