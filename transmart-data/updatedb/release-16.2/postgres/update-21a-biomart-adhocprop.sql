--
-- Update table biomart.ad_hoc_property
-- Make column bio_data_id not NULL
--

set search_path = biomart, pg_catalog;

ALTER TABLE IF EXISTS ONLY biomart.bio_ad_hoc_property ALTER COLUMN bio_data_id SET NOT NULL;

