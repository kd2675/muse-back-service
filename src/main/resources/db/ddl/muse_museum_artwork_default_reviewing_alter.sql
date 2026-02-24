-- Ensure newly created museum artworks start in REVIEWING status.

USE MUSE;

ALTER TABLE museum_artwork
    MODIFY COLUMN moderation_status VARCHAR(20) NOT NULL DEFAULT 'REVIEWING';
