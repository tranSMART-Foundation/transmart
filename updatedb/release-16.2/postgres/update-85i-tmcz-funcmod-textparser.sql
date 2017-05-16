--
-- Fix oracle syntax for psql
--

set search_path = tm_cz, pg_catalog;

-- not fully converted - some Oracle syntax remains

SET check_function_bodies = false;

DROP FUNCTION IF EXISTS tm_cz.text_parser(text);

\i ../../../ddl/postgres/tm_cz/functions/text_parser.sql


