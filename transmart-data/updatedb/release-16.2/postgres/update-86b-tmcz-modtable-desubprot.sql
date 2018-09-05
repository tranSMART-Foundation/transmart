--
-- intensity to float
-- wider subject_id
--

ALTER TABLE IF EXISTS tm_cz.de_subj_protein_data_release ALTER COLUMN intensity TYPE double precision;
ALTER TABLE IF EXISTS tm_cz.de_subj_protein_data_release ALTER COLUMN subject_id TYPE character varying(100);
ALTER TABLE IF EXISTS tm_cz.de_subj_protein_data_release ALTER COLUMN mean_intensity TYPE double precision;
ALTER TABLE IF EXISTS tm_cz.de_subj_protein_data_release ALTER COLUMN stddev_intensity TYPE double precision;
ALTER TABLE IF EXISTS tm_cz.de_subj_protein_data_release ALTER COLUMN median_intensity TYPE double precision;
ALTER TABLE IF EXISTS tm_cz.de_subj_protein_data_release ALTER COLUMN zscore TYPE double precision;
