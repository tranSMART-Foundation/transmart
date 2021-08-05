--
-- Name: patient_dimension; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE patient_dimension (
    patient_num int NOT NULL,
    vital_status_cd character varying(50),
    birth_date timestamp,
    death_date timestamp,
    sex_cd character varying(50),
    age_in_years_num int,
    language_cd character varying(50),
    race_cd character varying(50),
    marital_status_cd character varying(50),
    religion_cd character varying(50),
    zip_cd character varying(10),
    statecityzip_path character varying(700),
    income_cd character varying(50),
    patient_blob text,
    update_date timestamp,
    download_date timestamp,
    import_date timestamp,
    sourcesystem_cd character varying(50),
    upload_id int
);

--
-- Name: patient_dimension_pk; Type: CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY patient_dimension
    ADD CONSTRAINT patient_dimension_pk PRIMARY KEY (patient_num);

--
-- Name: pd_idx_dates; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX pd_idx_dates ON patient_dimension USING btree (patient_num, vital_status_cd, birth_date, death_date);

--
-- Name: PD_IDX_AllPatientDim; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX PD_IDX_AllPatientDim ON patient_dimension USING btree (patient_num, vital_status_cd, birth_date, death_date, sex_cd, age_in_years_num, language_cd, race_cd, marital_status_cd, religion_cd, zip_cd, income_cd);

--
-- Name: PD_IDX_StateCityZip; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX PD_IDX_StateCityZip ON patient_dimension USING btree (statecityzip_path, patient_num);

--
-- Name: PATD_UPLOADID_IDX; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX PATD_UPLOADID_IDX ON patient_dimension USING btree (upload_id);

--
-- Name: PATD_SOURCE_IDX; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX PATD_SOURCE_IDX ON patient_dimension USING btree (sourcesystem_cd);

--
-- Name: tf_trg_patient_dimension(); Type: FUNCTION; Schema: i2b2demodata; Owner: -
--
CREATE FUNCTION tf_trg_patient_dimension() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.patient_num is null then
	select nextval('i2b2demodata.seq_patient_num') into new.patient_num ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_patient_dimension; Type: TRIGGER; Schema: i2b2demodata; Owner: -
--
CREATE TRIGGER trg_patient_dimension BEFORE INSERT ON patient_dimension FOR EACH ROW EXECUTE PROCEDURE tf_trg_patient_dimension();

--
-- Name: seq_patient_num; Type: SEQUENCE; Schema: i2b2demodata; Owner: -
--
CREATE SEQUENCE seq_patient_num
    START WITH 135
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

