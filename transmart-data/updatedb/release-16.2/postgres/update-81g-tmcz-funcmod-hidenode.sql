--
-- Cleanup DOS line endings
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.i2b2_hide_node(character varying);

\i ../../../ddl/postgres/tm_cz/functions/i2b2_hide_node.sql

ALTER FUNCTION tm_cz.i2b2_hide_node(character varying) SET search_path TO tm_cz, i2b2metadata, i2b2demodata, pg_temp;

