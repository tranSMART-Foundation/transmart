--
-- Cleanup DOS line endings
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.czx_start_audit(character varying,character varying);

\i ../../../ddl/postgres/tm_cz/functions/czx_start_audit.sql

ALTER FUNCTION tm_cz.czx_start_audit(character varying,character varying) SET search_path TO tm_cz, pg_temp;

