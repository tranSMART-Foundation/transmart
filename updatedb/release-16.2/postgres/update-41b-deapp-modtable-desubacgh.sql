--
--
--

set search_path = deapp, pg_catalog;

ALTER TABLE IF EXISTS deapp.de_subject_acgh_data ADD COLUMN trial_source character varying(200);
