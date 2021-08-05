--
-- Name: de_subject_microarray_data; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_subject_microarray_data (
    trial_name character varying(100),
    probeset_id int,
    assay_id int,
    patient_id int,
    sample_id int,
    subject_id character varying(100),
    raw_intensity double precision,
    log_intensity double precision,
    zscore double precision,
    new_raw double precision,
    new_log double precision,
    new_zscore double precision,
    trial_source character varying(200),
    partition_id int
);

--
-- Name: de_microarray_data_idx1; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_microarray_data_idx1 ON de_subject_microarray_data USING btree (trial_name, assay_id, probeset_id);

--
-- Name: de_microarray_data_idx2; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_microarray_data_idx2 ON de_subject_microarray_data USING btree (assay_id, probeset_id);

--
-- Name: de_microarray_data_idx3; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_microarray_data_idx3 ON de_subject_microarray_data USING btree (assay_id);

--
-- Name: de_microarray_data_idx4; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_microarray_data_idx4 ON de_subject_microarray_data USING btree (probeset_id);

--
-- Name: de_microarray_data_idx5; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_microarray_data_idx5 ON de_subject_microarray_data USING btree (trial_source);

--
-- Name: de_microarray_data_idx6; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_microarray_data_idx6 ON de_subject_microarray_data USING btree (assay_id, patient_id, probeset_id);

