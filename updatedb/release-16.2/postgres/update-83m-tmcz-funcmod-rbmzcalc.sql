--
-- Common default for jobId
-- Cleanup partition code
-- Fix removal of existing data
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.i2b2_rbm_zscore_calc_new(character varying,character varying,bigint,character varying,bigint,character varying);
DROP FUNCTION IF EXISTS tm_cz.i2b2_rbm_zscore_calc_new(character varying,character varying,character varying,character varying,bigint,character varying,bigint,character varying);
DROP FUNCTION IF EXISTS tm_cz.i2b2_rbm_zscore_calc_new(character varying,character varying,character varying,numeric,character varying,bigint,character varying,bigint,character varying);

\i ../../../ddl/postgres/tm_cz/functions/i2b2_rbm_zscore_calc_new.sql


