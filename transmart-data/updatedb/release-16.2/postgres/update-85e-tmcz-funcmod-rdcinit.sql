--
-- Fix oracle syntax for psql
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.rdc_init_cap(text);

\i ../../../ddl/postgres/tm_cz/functions/rdc_init_cap.sql


