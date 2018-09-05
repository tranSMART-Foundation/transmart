--
-- Common default for jobId
-- Correct check for uppercase string
-- Return 0 instead of 1
-- Correct typo in message
-- Correct typo in comment
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.i2b2_load_study_metadata(numeric);

\i ../../../ddl/postgres/tm_cz/functions/i2b2_load_study_metadata.sql


