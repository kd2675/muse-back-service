-- Merge legacy REVIEWING contest-entry status into SUBMITTED.
-- Run once in environments that already contain contest_entry data.

UPDATE contest_entry
SET status = 'SUBMITTED'
WHERE status = 'REVIEWING';
