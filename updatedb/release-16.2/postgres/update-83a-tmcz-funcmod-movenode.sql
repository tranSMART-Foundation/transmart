--
-- Fix cz_error_handler calls
--

set search_path = tm_cz, pg_catalog;

DROP FUNCTION IF EXISTS tm_cz.i2b2_move_node(character varying,character varying,character varying,numeric);

\i ../../../ddl/postgres/tm_cz/functions/i2b2_move_node.sql


