-- Gallery artwork image asset table
-- Run this on existing databases that were created before artwork_asset was added.

use MUSE;

CREATE TABLE IF NOT EXISTS artwork_asset (
    artwork_id BIGINT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    image_url VARCHAR(2048) NOT NULL,
    create_date DATETIME NOT NULL,
    update_date DATETIME NOT NULL,
    CONSTRAINT fk_artwork_asset_artwork
        FOREIGN KEY (artwork_id) REFERENCES artwork(artwork_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
