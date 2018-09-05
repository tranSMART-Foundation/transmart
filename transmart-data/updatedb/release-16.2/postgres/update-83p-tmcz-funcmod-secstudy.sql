--
-- Fix oracle syntax for psql
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.i2b2_secure_study(text,bigint);

\i ../../../ddl/postgres/tm_cz/functions/i2b2_secure_study.sql


