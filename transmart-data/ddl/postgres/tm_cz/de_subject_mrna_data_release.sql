--
-- Name: de_subject_mrna_data_release; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE de_subject_mrna_data_release (
    trial_name character varying(100),
    probeset_id int,
    assay_id int,
    patient_id int,
    timepoint character varying(250),
    pvalue double precision,
    refseq character varying(50),
    subject_id character varying(100),
    raw_intensity int,
    mean_intensity double precision,
    stddev_intensity double precision,
    median_intensity double precision,
    log_intensity double precision,
    zscore double precision,
    sample_id int,
    release_study character varying(50)
);

