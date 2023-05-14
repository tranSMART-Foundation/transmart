--
-- Name: de_subject_rnaseq_data; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_subject_rnaseq_data (
    trial_source character varying(200),
    trial_name character varying(100),
    region_id int NOT NULL,
    assay_id int NOT NULL,
    patient_id int,
    readcount int,
    normalized_readcount double precision,
    log_normalized_readcount double precision,
    zscore double precision,
    partition_id int
);

--
-- Name: de_subject_rnaseq_data_pk; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_subject_rnaseq_data
    ADD CONSTRAINT de_subject_rnaseq_data_pk PRIMARY KEY (assay_id, region_id);

--
-- Name: de_subject_rnaseq_data_patient; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_subject_rnaseq_data_patient ON de_subject_rnaseq_data USING btree (patient_id);

--
-- Name: de_subject_rnaseq_data_region; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_subject_rnaseq_data_region ON de_subject_rnaseq_data USING btree (region_id);

--
-- Name: de_subj_rnaseq_region_id_fkey; Type: FK CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_subject_rnaseq_data
    ADD CONSTRAINT de_subj_rnaseq_region_id_fkey FOREIGN KEY (region_id) REFERENCES de_chromosomal_region(region_id);

