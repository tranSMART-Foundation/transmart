--
-- Fix oracle syntax for psql
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.bio_assay_analysis_uid(character varying);
DROP FUNCTION IF EXISTS tm_cz.bio_assay_analysis_uid(bigint);

\i ../../../ddl/postgres/tm_cz/functions/bio_assay_analysis_uid.sql


