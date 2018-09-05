--
-- Fix oracle syntax for psql
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.dropsyn();

\i ../../../ddl/postgres/tm_cz/functions/dropsyn.sql


