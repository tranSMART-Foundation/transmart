--
-- add table seachapp.import_xnat_config
--

set search_path = searchapp, pg_catalog;

\i ../../../ddl/postgres/searchapp/import_xnat_configuration.sql

ALTER TABLE IF EXISTS searchapp.import_xnat_configuration OWNER TO searchapp, SET TABLESPACE transmart;
ALTER INDEX IF EXISTS searchapp.import_xnat_config_pk SET TABLESPACE indx;

GRANT ALL ON TABLE searchapp.import_xnat_configuration to biomart_user;
GRANT ALL ON TABLE searchapp.import_xnat_configuration to tm_cz;
