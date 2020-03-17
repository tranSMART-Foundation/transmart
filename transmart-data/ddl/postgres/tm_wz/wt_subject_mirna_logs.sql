--
-- Name: wt_subject_mirna_logs; Type: TABLE; Schema: tm_wz; Owner: -
--
CREATE TABLE wt_subject_mirna_logs (
    probeset_id int,
    intensity_value double precision,
    pvalue double precision,
    num_calls int,
    assay_id int,
    patient_id int,
    sample_id int,
    subject_id character varying(100),
    trial_name character varying(100),
    timepoint character varying(250),
    log_intensity double precision
);

