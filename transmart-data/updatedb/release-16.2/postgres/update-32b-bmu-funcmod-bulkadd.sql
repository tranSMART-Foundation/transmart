--
-- Fix cz_error_handler calls
--

set search_path = biomart_user, pg_catalog;

DROP FUNCTION IF EXISTS biomart_user.i2b2_bulk_add_search_term(bigint);

\i ../../../ddl/postgres/biomart_user/functions/i2b2_bulk_add_search_term.sql

