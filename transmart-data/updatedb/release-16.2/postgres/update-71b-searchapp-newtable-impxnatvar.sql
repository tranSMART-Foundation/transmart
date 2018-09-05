--
-- add table seachapp.import_xnat_variable
--

set search_path = searchapp, pg_catalog;

\i ../../../ddl/postgres/searchapp/import_xnat_variable.sql

ALTER TABLE IF EXISTS searchapp.import_xnat_variable OWNER TO searchapp, SET TABLESPACE transmart;

ALTER INDEX IF EXISTS searchapp.import_xnat_var_pk SET TABLESPACE indx;

GRANT ALL ON TABLE searchapp.import_xnat_variable to tm_cz;
GRANT ALL ON TABLE searchapp.import_xnat_variable to biomart_user;
