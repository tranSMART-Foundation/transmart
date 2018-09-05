--
-- Fix oracle syntax for psql
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.set_bio_data_uid_path();

\i ../../../ddl/postgres/tm_cz/functions/set_bio_data_uid_path.sql


