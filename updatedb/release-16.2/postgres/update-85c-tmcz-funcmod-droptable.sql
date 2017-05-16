--
-- Fix oracle syntax for psql
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.drop_table(character varying,character varying);

\i ../../../ddl/postgres/tm_cz/functions/drop_table.sql


