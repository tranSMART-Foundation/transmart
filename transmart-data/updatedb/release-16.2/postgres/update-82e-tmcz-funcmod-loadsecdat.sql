--
-- Common default for jobId
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.i2b2_load_security_data(numeric);
DROP FUNCTION IF EXISTS tm_cz.i2b2_load_security_data(character varying,numeric);

\i ../../../ddl/postgres/tm_cz/functions/i2b2_load_security_data.sql

ALTER FUNCTION tm_cz.i2b2_load_security_data(numeric) SET search_path TO tm_cz, i2b2metadata, pg_temp;
ALTER FUNCTION tm_cz.i2b2_load_security_data(character varying,numeric) SET search_path TO tm_cz, i2b2metadata, pg_temp;


