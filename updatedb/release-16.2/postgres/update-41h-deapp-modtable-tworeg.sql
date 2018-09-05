--
--
--

set search_path = deapp, pg_catalog;

ALTER TABLE IF EXISTS deapp.de_two_region_event_gene RENAME CONSTRAINT two_region_event_gene_id_event_fk TO two_region_event_gene_fk;

