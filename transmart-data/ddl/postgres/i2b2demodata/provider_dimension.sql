--
-- Name: provider_dimension; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE provider_dimension (
    provider_id character varying(50) NOT NULL,
    provider_path character varying(700) NOT NULL,
    name_char character varying(850),
    provider_blob text,
    update_date timestamp,
    download_date timestamp,
    import_date timestamp,
    sourcesystem_cd character varying(50),
    upload_id int
);

--
-- Name: provider_dimension_pk; Type: CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY provider_dimension
    ADD CONSTRAINT provider_dimension_pk PRIMARY KEY (provider_path, provider_id);

--
-- Name: PD_IDX_NAME_CHAR; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX pd_idx_name_char ON provider_dimension USING btree (provider_id, name_char);

--
-- Name: PROD_UPLOADID_IDX; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX prod_uploadid_idx ON provider_dimension USING btree (upload_id);

