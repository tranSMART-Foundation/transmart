--
-- add index for i2b2_secure
--

set search_path = i2b2metadata, pg_catalog;

CREATE INDEX i2b2_secure_srcsystem_cd_idx ON i2b2_secure USING btree (sourcesystem_cd);

ALTER INDEX i2b2_secure_srcsystem_cd_idx SET TABLESPACE indx;

