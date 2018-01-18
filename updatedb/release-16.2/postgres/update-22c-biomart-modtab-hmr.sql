--
-- Expand biomart.heat_map_results
--

set search_path = biomart, pg_catalog;

ALTER TABLE IF EXISTS biomart.heat_map_results ALTER COLUMN subject_id TYPE character varying(100);
