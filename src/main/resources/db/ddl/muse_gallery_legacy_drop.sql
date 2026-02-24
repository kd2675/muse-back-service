-- Drop legacy gallery tables that were replaced by museum / museum_artwork.
-- Safe to run multiple times.

USE MUSE;

DROP TABLE IF EXISTS gallery_highlight;
DROP TABLE IF EXISTS gallery_category;
