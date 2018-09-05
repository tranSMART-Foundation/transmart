--
-- extend column subject_id
--

ALTER TABLE IF EXISTS tm_wz.wt_clinical_data_dups ALTER COLUMN subject_id TYPE character varying(100);
