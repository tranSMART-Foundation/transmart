--
-- Name: de_subject_mirna_data; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_subject_mirna_data (
    trial_source character varying(200),
    trial_name character varying(100),
    assay_id int NOT NULL,
    patient_id int,
    raw_intensity double precision,
    log_intensity double precision,
    probeset_id int NOT NULL,
    zscore double precision,
    partition_id int
);

--
-- Name: de_subject_mirna_data_pk; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_subject_mirna_data
    ADD CONSTRAINT de_subject_mirna_data_pk PRIMARY KEY (assay_id, probeset_id);

