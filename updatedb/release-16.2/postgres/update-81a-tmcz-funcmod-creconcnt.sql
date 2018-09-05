--
-- Rewritten for faster processing
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.i2b2_create_concept_counts(character varying,numeric);

\i ../../../ddl/postgres/tm_cz/functions/i2b2_create_concept_counts.sql

ALTER FUNCTION tm_cz.i2b2_create_concept_counts(character varying,numeric) SET search_path TO tm_cz, i2b2demodata, i2b2metadata, pg_temp;
