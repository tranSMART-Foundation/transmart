--
-- Fix cz_error_handler calls
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.upgrade_mrna_data(bigint);

\i ../../../ddl/postgres/tm_cz/functions/upgrade_mrna_data.sql


