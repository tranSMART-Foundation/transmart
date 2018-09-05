--
-- Correct typo in message
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.i2b2_load_omicsoft_annot(bigint,bigint);

\i ../../../ddl/postgres/tm_cz/functions/i2b2_load_omicsoft_annot.sql

