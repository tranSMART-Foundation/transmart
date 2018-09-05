--
-- Fix oracle syntax for psql
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.czf_init_cap(character varying);

\i ../../../ddl/postgres/tm_cz/functions/czf_init_cap.sql


