--
-- Fix oracle syntax for psql
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.bio_asy_analysis_pltfm_uid(character varying);

\i ../../../ddl/postgres/tm_cz/functions/bio_asy_analysis_pltfm_uid.sql


