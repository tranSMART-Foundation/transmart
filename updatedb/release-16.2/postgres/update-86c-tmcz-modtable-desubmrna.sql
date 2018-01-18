--
-- longer subject_id
--

ALTER TABLE IF EXISTS tm_cz.de_subject_mrna_data_release ALTER COLUMN subject_id TYPE character varying(100);
