-- Apply when `contest_entry_ledger` table already exists.
-- Guards duplicate voting rows for the same user/contest/entry pair.

USE MUSE;

ALTER TABLE contest_entry_ledger
    ADD CONSTRAINT uk_contest_entry_ledger_vote
        UNIQUE (artist_id, contest_id, reason, ref_id);

