--
-- Update table biomart.bio_data_platform
-- Make column bio_data_id NOT NULL
-- Make column bio_assay_platform_id NOT NULL
--

set search_path = biomart, pg_catalog;

ALTER TABLE IF EXISTS ONLY biomart.bio_data_platform ALTER COLUMN bio_data_id SET NOT NULL;
ALTER TABLE IF EXISTS ONLY biomart.bio_data_platform ALTER COLUMN bio_assay_platform_id SET NOT NULL;
