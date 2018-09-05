--
-- Cleanup DOS line endings
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.find_user(character varying);

\i ../../../ddl/postgres/tm_cz/functions/find_user.sql

