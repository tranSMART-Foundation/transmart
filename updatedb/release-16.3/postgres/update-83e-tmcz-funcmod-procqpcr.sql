--
-- Missing Age set to NULL
--
set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.i2b2_process_qpcr_mirna_data(character varying, character varying, character varying, character varying, numeric, character varying, numeric, character varying);

\i ../../../ddl/postgres/tm_cz/functions/i2b2_process_qpcr_mirna_data.sql

ALTER FUNCTION tm_cz.i2b2_process_qpcr_mirna_data(character varying, character varying, character varying, character varying, numeric, character varying, numeric, character varying) SET search_path TO tm_cz, tm_lz, tm_wz, i2b2demodata, i2b2metadata, deapp, pg_temp;
