--
-- Cleanup DOS line endings
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.czx_table_index_maint(character varying,character varying,character varying,numeric);

\i ../../../ddl/postgres/tm_cz/functions/czx_table_index_maint.sql

ALTER FUNCTION tm_cz.czx_table_index_maint(character varying,character varying,character varying,numeric) SET search_path TO tm_cz, tm_lz, tm_wz, deapp, i2b2demodata, pg_temp;

