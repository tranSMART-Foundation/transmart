--
-- Fix cz_error_handler calls
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.cz_audit_example(bigint);

\i ../../../ddl/postgres/tm_cz/functions/cz_audit_example.sql

