--
-- Rewritten for faster processing
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.i2b2_delete_1_node(character varying);

\i ../../../ddl/postgres/tm_cz/functions/i2b2_delete_1_node.sql

ALTER FUNCTION tm_cz.i2b2_delete_1_node(character varying) SET search_path TO tm_cz, i2b2demodata, i2b2metadata, pg_temp;

