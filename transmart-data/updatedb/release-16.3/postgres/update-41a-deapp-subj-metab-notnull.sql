--
-- allow NULL data values
--
ALTER TABLE IF EXISTS ONLY deapp.de_subject_metabolomic_data ALTER COLUMN raw_intensity DROP NOT NULL;
ALTER TABLE IF EXISTS ONLY deapp.de_subject_metabolomic_data ALTER COLUMN log_intensity DROP NOT NULL;
ALTER TABLE IF EXISTS ONLY deapp.de_subject_metabolomic_data ALTER COLUMN zscore DROP NOT NULL;
