--
-- integer to float
--

ALTER TABLE IF EXISTS tm_wz.wt_subject_rna_calcs ALTER COLUMN mean_intensity TYPE double precision;
ALTER TABLE IF EXISTS tm_wz.wt_subject_rna_calcs ALTER COLUMN median_intensity TYPE double precision;
ALTER TABLE IF EXISTS tm_wz.wt_subject_rna_calcs  ALTER COLUMN stddev_intensity TYPE double precision;
