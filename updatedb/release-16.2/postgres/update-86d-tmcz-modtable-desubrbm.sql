--
-- integer to float
--

ALTER TABLE IF EXISTS tm_cz.de_subject_rbm_data_release ALTER COLUMN log_intensity TYPE double precision;
ALTER TABLE IF EXISTS tm_cz.de_subject_rbm_data_release ALTER COLUMN mean_intensity TYPE double precision;
ALTER TABLE IF EXISTS tm_cz.de_subject_rbm_data_release ALTER COLUMN stddev_intensity TYPE double precision;
ALTER TABLE IF EXISTS tm_cz.de_subject_rbm_data_release ALTER COLUMN median_intensity TYPE double precision;
ALTER TABLE IF EXISTS tm_cz.de_subject_rbm_data_release ALTER COLUMN zscore TYPE double precision;
