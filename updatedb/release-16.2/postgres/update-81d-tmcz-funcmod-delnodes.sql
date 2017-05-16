--
-- Rewritten for faster processing
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.i2b2_delete_all_nodes(character varying,numeric);

\i ../../../ddl/postgres/tm_cz/functions/i2b2_delete_all_nodes.sql

ALTER FUNCTION tm_cz.i2b2_delete_all_nodes(character varying,numeric) SET search_path TO tm_cz, i2b2demodata, i2b2metadata, pg_temp;

