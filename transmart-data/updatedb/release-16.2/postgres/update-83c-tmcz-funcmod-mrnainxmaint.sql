--
-- Fix cz_error_handler calls
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.i2b2_mrna_index_maint(text,text,bigint);

\i ../../../ddl/postgres/tm_cz/functions/i2b2_mrna_index_maint.sql


