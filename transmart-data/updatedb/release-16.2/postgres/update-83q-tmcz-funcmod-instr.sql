--
-- Cleanup DOS line endings
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.instr(character varying,character varying,integer,integer);

\i ../../../ddl/postgres/tm_cz/functions/instr.sql


