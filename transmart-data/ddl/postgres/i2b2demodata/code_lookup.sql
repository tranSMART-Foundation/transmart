--
-- Name: code_lookup; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE code_lookup (
    table_cd character varying(100) NOT NULL,
    column_cd character varying(100) NOT NULL,
    code_cd character varying(50) NOT NULL,
    name_char character varying(650),
    lookup_blob text,
    upload_date timestamp,
    update_date timestamp,
    download_date timestamp,
    import_date timestamp,
    sourcesystem_cd character varying(50),
    upload_id int
);

--
-- Name: code_lookup_pk; Type: CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY code_lookup
    ADD CONSTRAINT code_lookup_pk PRIMARY KEY (table_cd, column_cd, code_cd);

--
-- Name: cl_idx_name_char; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX cl_idx_name_char ON code_lookup USING btree (name_char);

--
-- Name: cl_idx_uploadid; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX cl_idx_uploadid ON code_lookup USING btree (upload_id);

