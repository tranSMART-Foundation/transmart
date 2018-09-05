--
-- Fix cz_error_handler calls
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.rdc_reload_mrna_data(text,text,text,bigint,bigint);

\i ../../../ddl/postgres/tm_cz/functions/rdc_reload_mrna_data.sql


