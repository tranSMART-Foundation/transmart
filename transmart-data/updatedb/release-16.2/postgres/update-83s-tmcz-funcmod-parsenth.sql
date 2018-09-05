--
-- Cleanup DOS line endings
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.parse_nth_value(character varying,numeric,character varying);

\i ../../../ddl/postgres/tm_cz/functions/parse_nth_value.sql


