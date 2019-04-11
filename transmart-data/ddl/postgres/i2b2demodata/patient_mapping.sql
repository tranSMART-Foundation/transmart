--
-- Name: patient_mapping; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE patient_mapping (
    patient_ide character varying(200) NOT NULL,
    patient_ide_source character varying(50) NOT NULL,
    patient_num int NOT NULL,
    patient_ide_status character varying(50),
    project_id character varying(50) NOT NULL,
    upload_date timestamp,
    update_date timestamp,
    download_date timestamp,
    import_date timestamp,
    sourcesystem_cd character varying(50),
    upload_id int
);

--
-- Name: patient_mapping_pk; Type: CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY patient_mapping
    ADD CONSTRAINT patient_mapping_pk PRIMARY KEY (patient_ide, patient_ide_source, project_id);

--
-- Name: pm_uploadid_idx; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX pm_uploadid_idx ON patient_mapping USING btree (upload_id);

--
-- Name: pm_patnum_idx; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX pm_patnum_idx ON patient_mapping USING btree (patient_num);

--
-- Name: pm_encpnum_idx; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX pm_encpnum_idx ON patient_mapping USING btree (patient_ide, patient_ide_source, patient_num);

