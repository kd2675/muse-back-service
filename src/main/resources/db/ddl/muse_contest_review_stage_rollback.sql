-- Rollback script for persisted contest review stage.
-- Safe to run multiple times.

USE MUSE;

SET @review_stage_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'contest'
      AND COLUMN_NAME = 'review_stage'
);

SET @ddl := IF(
    @review_stage_exists > 0,
    'ALTER TABLE contest DROP COLUMN review_stage',
    'SELECT ''review_stage column does not exist'' AS message'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
