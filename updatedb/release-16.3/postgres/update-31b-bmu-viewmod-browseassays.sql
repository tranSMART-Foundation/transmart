--
-- Add biosource to browse_assays_view
-- others unchanged
--
set search_path = biomart_user, pg_catalog;

DROP VIEW IF EXISTS biomart_user.browse_analyses_view;
DROP VIEW IF EXISTS biomart_user.browse_folders_view;
DROP VIEW IF EXISTS biomart_user.browse_assays_view;
DROP VIEW IF EXISTS biomart_user.browse_programs_view;
DROP VIEW IF EXISTS biomart_user.browse_studies_view;

\i ../../../ddl/postgres/biomart_user/_cross.sql

ALTER VIEW IF EXISTS biomart_user.browse_analyses_view OWNER TO biomart_user;
ALTER VIEW IF EXISTS biomart_user.browse_folders_view OWNER TO biomart_user;
ALTER VIEW IF EXISTS biomart_user.browse_assays_view OWNER TO biomart_user;
ALTER VIEW IF EXISTS biomart_user.browse_programs_view OWNER TO biomart_user;
ALTER VIEW IF EXISTS biomart_user.browse_studies_view OWNER TO biomart_user;
