--
-- Name: wt_subject_metabolomics_med; Type: TABLE; Schema: tm_wz; Owner: -
--
CREATE TABLE wt_subject_metabolomics_med (
    probeset character varying(500),
    intensity_value double precision,
    log_intensity double precision,
    assay_id int,
    patient_id int,
    sample_id int,
    subject_id character varying(100),
    trial_name character varying(50),
    timepoint character varying(100),
    pvalue double precision,
    num_calls int,
    mean_intensity double precision,
    stddev_intensity double precision,
    median_intensity double precision,
    zscore double precision
);

