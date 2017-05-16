--
-- Fix oracle syntax for psql
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.jnj_init_cap(text);

\i ../../../ddl/postgres/tm_cz/functions/jnj_init_cap.sql


