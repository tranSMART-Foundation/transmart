--
-- Name: de_subject_microarray_data_new; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_subject_microarray_data_new (
    trial_source character varying(200),
    trial_name character varying(100),
    probeset_id int,
    assay_id int,
    patient_id int,
    raw_intensity double precision,
    log_intensity double precision,
    zscore double precision
);

