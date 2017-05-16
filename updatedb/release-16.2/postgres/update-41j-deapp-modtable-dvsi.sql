--
--
--

SET search_path = deapp, pg_catalog;

ALTER TABLE IF EXISTS deapp.de_variant_subject_idx ALTER COLUMN subject_id TYPE character varying(100);
