--
--
--

set search_path = deapp, pg_catalog;

ALTER TABLE IF EXISTS deapp.de_subject_protein_data ADD COLUMN timepoint character varying(100);
