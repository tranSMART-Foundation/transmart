--
-- extend subject_id
-- integer to float
--

ALTER TABLE IF EXISTS tm_wz.wt_subject_microarray_logs ALTER COLUMN subject_id TYPE character varying(100);
ALTER TABLE IF EXISTS tm_wz.wt_subject_microarray_logs ALTER COLUMN intensity_value TYPE double precision;
