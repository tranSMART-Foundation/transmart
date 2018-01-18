--
-- Cleanup DOS line endings
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.czx_info_handler(numeric,numeric,numeric,character varying,character varying,character varying);

\i ../../../ddl/postgres/tm_cz/functions/czx_info_handler.sql

ALTER FUNCTION tm_cz.czx_info_handler(numeric,numeric,numeric,character varying,character varying,character varying) SET search_path TO tm_cz, pg_temp;

