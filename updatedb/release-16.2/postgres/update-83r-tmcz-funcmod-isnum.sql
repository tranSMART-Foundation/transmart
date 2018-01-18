--
-- Cleanup DOS line endings
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.is_numeric(character varying);

\i ../../../ddl/postgres/tm_cz/functions/is_numeric.sql


