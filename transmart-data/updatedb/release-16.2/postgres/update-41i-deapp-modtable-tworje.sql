--
--
--

set search_path = deapp, pg_catalog;

ALTER TABLE IF EXISTS deapp.de_two_region_junction_event RENAME CONSTRAINT two_region_junction_event_id_event_fk TO two_region_jn_event_ie_fk;

ALTER TABLE IF EXISTS deapp.de_two_region_junction_event RENAME CONSTRAINT two_region_junction_event_id_junction_fk TO two_region_jn_event_ij_fk;

ALTER TABLE IF EXISTS deapp.de_two_region_junction_event RENAME CONSTRAINT two_region_junction_event_id_pk TO two_region_jn_event_id_pk;


