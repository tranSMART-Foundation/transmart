--
-- extend column race_cd
--

ALTER TABLE IF EXISTS tm_wz.wt_subject_mbolomics_probeset ALTER COLUMN intensity_value TYPE double precision;
ALTER TABLE IF EXISTS tm_wz.wt_subject_mbolomics_probeset ALTER COLUMN pvalue TYPE double precision;
