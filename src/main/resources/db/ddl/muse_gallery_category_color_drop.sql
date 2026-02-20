-- Drop obsolete color columns from gallery_category.
-- Safe to run multiple times in MySQL 8.0+.

use MUSE;

ALTER TABLE gallery_category
    DROP COLUMN color_from,
    DROP COLUMN color_to;
