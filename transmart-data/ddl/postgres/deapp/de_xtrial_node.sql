--
-- Name: de_xtrial_node; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_xtrial_node (
    modifier_path character varying(700) NOT NULL,
    modifier_cd character varying(50),
    name_char character varying(2000),
    modifier_blob text,
    update_date timestamp,
    download_date timestamp,
    import_date timestamp,
    sourcesystem_cd character varying(50),
    upload_id int,
    modifier_level bigint,
    modifier_node_type character varying(10)

);

--
-- Name: de_xtrial_node_pk; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_xtrial_node
    ADD CONSTRAINT de_xtrial_node_pk PRIMARY KEY (modifier_path);

--
-- Name: xn_idx_uploadid; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX xn_idx_uploadid ON de_xtrial_node USING btree (upload_id);

