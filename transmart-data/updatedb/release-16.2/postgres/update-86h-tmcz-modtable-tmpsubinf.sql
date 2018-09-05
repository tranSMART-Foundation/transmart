--
-- extend column race_cd
--

ALTER TABLE IF EXISTS tm_cz.tmp_subject_info ALTER COLUMN race_cd TYPE character varying(100);
