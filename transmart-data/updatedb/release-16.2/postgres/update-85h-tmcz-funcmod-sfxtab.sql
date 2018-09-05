--
-- Fix oracle syntax for psql
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.sf_xtab(character varying,numeric,character varying,bigint);

\i ../../../ddl/postgres/tm_cz/functions/sf_xtab.sql


