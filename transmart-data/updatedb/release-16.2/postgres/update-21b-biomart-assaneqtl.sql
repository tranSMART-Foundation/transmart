--
-- Update dable biomart.bio_assay_analysis_eqtl
-- Make column bio_assay_analysis_id not NULL
--

set search_path = biomart, pg_catalog;

ALTER TABLE IF EXISTS ONLY biomart.bio_assay_analysis_eqtl ALTER COLUMN bio_assay_analysis_id SET NOT NULL;

