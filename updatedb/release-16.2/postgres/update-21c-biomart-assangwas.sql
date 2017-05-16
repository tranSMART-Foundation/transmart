--
-- Update dable biomart.bio_assay_analysis_gwas
-- Make column bio_assay_analysis_id not NULL
--

set search_path = biomart, pg_catalog;

ALTER TABLE IF EXISTS ONLY biomart.bio_assay_analysis_gwas ALTER COLUMN bio_assay_analysis_id SET NOT NULL;

ALTER TABLE IF EXISTS ONLY biomart.bio_assay_analysis_gwas ADD COLUMN effect_allele character varying(100);
ALTER TABLE IF EXISTS ONLY biomart.bio_assay_analysis_gwas ADD COLUMN other_allele character varying(100);
ALTER TABLE IF EXISTS ONLY biomart.bio_assay_analysis_gwas ADD COLUMN beta character varying(100);
ALTER TABLE IF EXISTS ONLY biomart.bio_assay_analysis_gwas ADD COLUMN standard_error character varying(100);

