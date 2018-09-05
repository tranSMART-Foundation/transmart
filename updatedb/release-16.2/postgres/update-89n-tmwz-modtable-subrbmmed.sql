--
-- extend subject_id
-- integer to float
--

ALTER TABLE IF EXISTS tm_wz.wt_subject_rbm_med ALTER COLUMN subject_id TYPE character varying(100);
ALTER TABLE IF EXISTS tm_wz.wt_subject_rbm_med ALTER COLUMN intensity_value TYPE double precision;
ALTER TABLE IF EXISTS tm_wz.wt_subject_rbm_med ALTER COLUMN log_intensity TYPE double precision;
ALTER TABLE IF EXISTS tm_wz.wt_subject_rbm_med ALTER COLUMN mean_intensity TYPE double precision;
ALTER TABLE IF EXISTS tm_wz.wt_subject_rbm_med ALTER COLUMN stddev_intensity TYPE double precision;
ALTER TABLE IF EXISTS tm_wz.wt_subject_rbm_med ALTER COLUMN median_intensity TYPE double precision;
ALTER TABLE IF EXISTS tm_wz.wt_subject_rbm_med ALTER COLUMN zscore TYPE double precision;
