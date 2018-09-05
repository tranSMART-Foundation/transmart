--
-- extend subject_id column
--

set search_path = deapp, pg_catalog;

ALTER TABLE IF EXISTS deapp.de_subject_microarray_logs ALTER COLUMN subject_id TYPE character varying(100);
