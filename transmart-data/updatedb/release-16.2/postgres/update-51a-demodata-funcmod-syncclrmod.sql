--
-- Fix oracle syntax for psql
--

set search_path = i2b2demodata, pg_catalog;

DROP FUNCTION IF EXISTS i2b2demodata.sync_clear_modifier_table(character varying,character varying,numeric);

\i ../../../ddl/postgres/i2b2demodata/functions/sync_clear_modifier_table.sql


