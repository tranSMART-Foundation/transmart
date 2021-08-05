--
-- Name: de_subject_sample_mapping; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_subject_sample_mapping (
    patient_id int,
    site_id character varying(100),
    subject_id character varying(100),
    subject_type character varying(100),
    concept_code character varying(1000),
    assay_id int NOT NULL,
    patient_uid character varying(50),
    sample_type character varying(100),
    assay_uid character varying(100),
    trial_name character varying(100),
    timepoint character varying(250),
    timepoint_cd character varying(50),
    sample_type_cd character varying(50),
    tissue_type_cd character varying(50),
    platform character varying(50),
    platform_cd character varying(50),
    tissue_type character varying(100),
    data_uid character varying(100),
    gpl_id character varying(50),
    rbm_panel character varying(50),
    sample_id int,
    sample_cd character varying(200),
    category_cd character varying(1000),
    source_cd character varying(200),
    omic_source_study character varying(200),
    omic_patient_num int,
    omic_patient_id int,
    partition_id int
);

--
-- Name: de_subject_smpl_mpng_idx1; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_subject_smpl_mpng_idx1 ON de_subject_sample_mapping USING btree (timepoint, patient_id, trial_name);

--
-- Name: de_subject_smpl_mpng_idx2; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_subject_smpl_mpng_idx2 ON de_subject_sample_mapping USING btree (patient_id, timepoint_cd, platform_cd, assay_id, trial_name);

--
-- Name: de_subject_smpl_mpng_idx3; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_subject_smpl_mpng_idx3 ON de_subject_sample_mapping USING btree (sample_type_cd);

--
-- Name: idx_de_subj_smpl_trial_ccode; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX idx_de_subj_smpl_trial_ccode ON de_subject_sample_mapping USING btree (trial_name, concept_code);

