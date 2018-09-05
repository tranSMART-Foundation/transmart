--
-- Missing Age set to NULL
--
set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.i2b2_load_sample_categories(bigint);

\i ../../../ddl/postgres/tm_cz/functions/i2b2_load_sample_categories.sql

