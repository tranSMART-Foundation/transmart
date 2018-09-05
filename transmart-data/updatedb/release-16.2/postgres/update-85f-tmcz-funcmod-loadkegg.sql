--
-- Fix oracle syntax for psql
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.load_kegg_content_data();

\i ../../../ddl/postgres/tm_cz/functions/load_kegg_content_data.sql


