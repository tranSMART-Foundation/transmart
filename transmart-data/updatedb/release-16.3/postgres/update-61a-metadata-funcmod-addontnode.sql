--
-- fix typo in comment, code is unchanged
--
set search_path = i2b2metadata, pg_catalog;

DROP FUNCTION IF EXISTS i2b2metadata.add_ontology_node(character varying, character varying, character varying, character varying, character varying);

\i ../../../ddl/postgres/i2b2metadata/functions/add_ontology_node.sql
