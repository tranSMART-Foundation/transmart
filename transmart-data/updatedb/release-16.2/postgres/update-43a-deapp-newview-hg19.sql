--
-- New view
--

set search_path = deapp, pg_catalog;

DROP VIEW IF EXISTS deapp.de_snp_info_hg19_mv;

\i ../../../ddl/postgres/deapp/views/de_snp_info_hg19_mv.sql

ALTER MATERIALIZED VIEW deapp.de_snp_info_hg19_mv OWNER TO deapp;

GRANT SELECT ON TABLE deapp.de_snp_info_hg19_mv TO biomart_user;
