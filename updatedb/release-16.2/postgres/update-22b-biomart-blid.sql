--
-- require value for biomart.bio_lit_inh_data.bio_lit_ref_data_id
--

set search_path = biomart, pg_catalog;

ALTER TABLE biomart.bio_lit_inh_data ALTER COLUMN bio_lit_ref_data_id SET NOT NULL;
