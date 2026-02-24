-- Drop contest.status column after phase-based status unification.
-- MySQL 8.0 compatible (works even when the column is already removed).

USE MUSE;

SET @has_status_column := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'contest'
      AND COLUMN_NAME = 'status'
);

SET @drop_sql := IF(
    @has_status_column > 0,
    'ALTER TABLE contest DROP COLUMN status',
    'SELECT ''contest.status already removed'' AS message'
);

PREPARE stmt FROM @drop_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
