--
-- intensity and zcore floating point numbers
--

set search_path = deapp, pg_catalog;

ALTER TABLE IF EXISTS deapp.de_subject_proteomics_data ALTER COLUMN intensity TYPE double precision;
ALTER TABLE IF EXISTS deapp.de_subject_proteomics_data ALTER COLUMN zscore TYPE double precision;
