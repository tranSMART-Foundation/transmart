--
-- Missing Age set to NULL
--
set search_path = tm_lz, pg_catalog;

DROP FUNCTION IF EXISTS tm_lz.i2b2_process_qpcr_mirna_data(character varying, character varying, character varying, character varying, character varying, numeric, character varying, numeric);

\i ../../../ddl/postgres/tm_lz/functions/i2b2_process_qpcr_mirna_data.sql
