--
-- add table seachapp.xnat_subject
--

set search_path = searchapp, pg_catalog;

\i ../../../ddl/postgres/searchapp/xnat_subject.sql

ALTER TABLE IF EXISTS searchapp.xnat_subject OWNER TO searchapp, SET TABLESPACE transmart;

ALTER INDEX IF EXISTS searchapp.xnat_subject_pk SET TABLESPACE indx;

GRANT ALL ON TABLE searchapp.xnat_subject to tm_cz;
GRANT ALL ON TABLE searchapp.xnat_subject to biomart_user;
