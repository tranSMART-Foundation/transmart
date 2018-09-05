--
-- extend column race_cd
--

ALTER TABLE IF EXISTS tm_cz.patient_dimension_release ALTER COLUMN race_cd TYPE character varying(100);
