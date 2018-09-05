--
-- Cleanup DOS line endings
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.czx_write_audit(numeric,character varying,character varying,character varying,numeric,numeric,character varying);

\i ../../../ddl/postgres/tm_cz/functions/czx_write_audit.sql

ALTER FUNCTION tm_cz.czx_write_audit(numeric,character varying,character varying,character varying,numeric,numeric,character varying) SET search_path TO tm_cz, pg_temp;

