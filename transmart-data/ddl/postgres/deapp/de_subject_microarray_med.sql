--
-- Name: de_subject_microarray_med; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_subject_microarray_med (
    probeset character varying(50),
    raw_intensity double precision,
    log_intensity double precision,
    gene_symbol character varying(100),
    assay_id int,
    patient_id int,
    subject_id character varying(100),
    trial_name character varying(100),
    timepoint character varying(250),
    pvalue double precision,
    refseq character varying(50),
    mean_intensity double precision,
    stddev_intensity double precision,
    median_intensity double precision,
    zscore double precision
);

