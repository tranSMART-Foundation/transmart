--
-- syntax errors in cz_write_audit calls
--
set search_path = biomart_user, pg_catalog;

DROP FUNCTION IF EXISTS biomart_user.i2b2_metabolomics_zscore_calc(character varying, character varying, character varying, numeric, character varying, numeric);

\i ../../../ddl/postgres/biomart_user/functions/i2b2_metabolomics_zscore_calc.sql

