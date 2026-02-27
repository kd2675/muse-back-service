-- Reset muse domain data only (keep user/account data in other schemas intact).
-- Safe to run multiple times.

USE MUSE;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE contest_entry_ledger;
TRUNCATE TABLE contest_entry_credit;
TRUNCATE TABLE contest_entry;
TRUNCATE TABLE contest_rule;

TRUNCATE TABLE museum_artwork;
TRUNCATE TABLE museum;

TRUNCATE TABLE profile_award;
TRUNCATE TABLE profile_portfolio;
TRUNCATE TABLE profile_stat;
TRUNCATE TABLE profile_artist;

TRUNCATE TABLE home_pick;
TRUNCATE TABLE artwork_asset;
TRUNCATE TABLE artwork;

TRUNCATE TABLE home_hero;
TRUNCATE TABLE contest;

SET FOREIGN_KEY_CHECKS = 1;
