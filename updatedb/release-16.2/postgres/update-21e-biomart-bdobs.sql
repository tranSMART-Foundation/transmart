--
-- Update table biomart.bio_data_observation
-- Make column bio_data_id NOT NULL
-- Make column bio_observation_id NOT NULL
--

ALTER TABLE IF EXISTS ONLY biomart.bio_data_observation ALTER COLUMN bio_data_id SET NOT NULL;
ALTER TABLE IF EXISTS ONLY biomart.bio_data_observation ALTER COLUMN bio_observation_id SET NOT NULL;
