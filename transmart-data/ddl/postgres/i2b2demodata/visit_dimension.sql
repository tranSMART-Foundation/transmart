--
-- Name: visit_dimension; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE visit_dimension (
    encounter_num int NOT NULL,
    patient_num int NOT NULL,
    active_status_cd character varying(50),
    start_date timestamp,
    end_date timestamp,
    inout_cd character varying(50),
    location_cd character varying(50),
    location_path character varying(900),
    length_of_stay int,
    visit_blob text,
    update_date timestamp,
    download_date timestamp,
    import_date timestamp,
    sourcesystem_cd character varying(50),
    upload_id int
);

--
-- Name: visit_dimension_pk; Type: CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY visit_dimension
    ADD CONSTRAINT visit_dimension_pk PRIMARY KEY (encounter_num, patient_num);

--
-- Name: VISITDIM_STD_EDD_IDX; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX VISITDIM_STD_EDD_IDX ON visit_dimension USING btree (start_date, end_date);

--
-- Name: INDEX VD_IDX_AllVisitDim; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX VD_IDX_AllVisitDim ON visit_dimension USING btree (encounter_num, patient_num, inout_cd, location_cd, start_date, length_of_stay, end_date);
-- i2b2 differs on oracle: uses location_path
 
--
-- Name: VD_UPLOADID_IDX; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX VD_UPLOADID_IDX ON visit_dimension USING btree (upload_id);
