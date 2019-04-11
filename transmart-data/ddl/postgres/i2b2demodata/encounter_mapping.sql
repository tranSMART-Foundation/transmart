--
-- Name: encounter_mapping; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE encounter_mapping (
    encounter_ide character varying(200) NOT NULL,
    encounter_ide_source character varying(50) NOT NULL,
    project_id character varying(50) NOT NULL,
    encounter_num int NOT NULL,
    patient_ide character varying(200) NOT NULL,
    patient_ide_source character varying(50) NOT NULL,
    encounter_ide_status character varying(50),
    upload_date timestamp,
    update_date timestamp,
    download_date timestamp,
    import_date timestamp,
    sourcesystem_cd character varying(50),
    upload_id int
);

--
-- Name: encounter_mapping_pk; Type: CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY encounter_mapping
    ADD CONSTRAINT encounter_mapping_pk PRIMARY KEY (encounter_ide, encounter_ide_source, project_id, patient_ide, patient_ide_source);

--
-- Name: em_uploadid_idx; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX em_uploadid_idx ON encounter_mapping USING btree (upload_id);

--
-- Name: em_idx_encpath; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX em_idx_encpath ON encounter_mapping USING btree (encounter_ide, encounter_ide_source, patient_ide, patient_ide_source, encounter_num);

--
-- Name: em_encnum_idx; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX em_encnum_idx ON encounter_mapping USING btree (encounter_num);


