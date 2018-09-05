--
-- Fix cz_error_handler calls
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.i2b2_mirna_zscore_calc(character varying,character varying,numeric,character varying,numeric,character varying);

\i ../../../ddl/postgres/tm_cz/functions/i2b2_mirna_zscore_calc.sql


