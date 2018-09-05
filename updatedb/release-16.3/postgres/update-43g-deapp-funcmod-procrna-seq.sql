--
-- Missing Age set to NULL
--
set search_path = deapp, pg_catalog;

DROP FUNCTION IF EXISTS deapp.i2b2_process_rna_seq_data(character varying, character varying, character varying, character varying, numeric, character varying, numeric);

\i ../../../ddl/postgres/deapp/functions/i2b2_process_rna_seq_data.sql

