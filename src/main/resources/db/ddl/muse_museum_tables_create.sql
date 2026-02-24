-- Create museum and museum_artwork tables for user-managed gallery model.

USE MUSE;

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
