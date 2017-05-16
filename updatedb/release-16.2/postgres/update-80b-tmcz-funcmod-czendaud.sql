--
-- Cleanup DOS line endings
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.cz_end_audit(numeric,character varying);

\i ../../../ddl/postgres/tm_cz/functions/cz_end_audit.sql

ALTER FUNCTION cz_end_audit(numeric, character varying) SET search_path TO tm_cz, pg_temp;
