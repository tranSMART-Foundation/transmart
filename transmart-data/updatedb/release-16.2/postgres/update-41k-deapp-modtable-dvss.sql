--
--
--

set search_path = deapp, pg_catalog;

DROP VIEW IF EXISTS deapp.de_variant_summary_detail_gene;

ALTER TABLE IF EXISTS deapp.de_variant_subject_summary ALTER COLUMN subject_id TYPE character varying(100);

\i ../../../ddl/postgres/deapp/views/de_variant_summary_detail_gene.sql

ALTER VIEW IF EXISTS deapp.de_variant_summary_detail_gene OWNER TO deapp;
