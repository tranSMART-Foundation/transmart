--
-- extend column subject_id
--

ALTER TABLE IF EXISTS tm_lz.lt_src_subj_enroll_date ALTER COLUMN subject_id TYPE character varying(100);

ALTER TABLE IF EXISTS tm_lz.lz_src_subj_enroll_date ALTER COLUMN subject_id TYPE character varying(100);
