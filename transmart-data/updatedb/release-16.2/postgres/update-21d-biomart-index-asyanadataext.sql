--
-- Update table biomart.bio_asy_analysis_data_ext
-- add primary key
--

set search_path = biomart, pg_catalog;

ALTER TABLE IF EXISTS ONLY biomart.bio_asy_analysis_data_ext
     ADD CONSTRAINT bio_asy_analysis_data_id_pk PRIMARY KEY (bio_asy_analysis_data_id);

ALTER INDEX IF EXISTS bio_asy_analysis_data_id_pk SET TABLESPACE indx;
