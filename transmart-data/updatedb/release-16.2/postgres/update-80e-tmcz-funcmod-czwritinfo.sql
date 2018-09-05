--
-- Cleanup DOS line endings
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.cz_write_info(numeric,numeric,numeric,character varying,character varying);

\i ../../../ddl/postgres/tm_cz/functions/cz_write_info.sql

ALTER FUNCTION tm_cz.cz_write_info(numeric,numeric,numeric,character varying,character varying) SET search_path TO tm_cz, pg_temp;

