--
-- Name: de_subject_rbm_data; Type: TABLE; Schema: tm_wz; Owner: -
--
CREATE TABLE de_subject_rbm_data (
    trial_name character varying(100),
    antigen_name character varying(100),
    n_value int,
    patient_id int,
    gene_symbol character varying(100),
    gene_id int,
    assay_id int,
    normalized_value double precision,
    concept_cd character varying(100),
    timepoint character varying(250),
    data_uid character varying(100),
    value decimal(18,4),
    log_intensity double precision,
    mean_intensity double precision,
    stddev_intensity double precision,
    median_intensity double precision,
    zscore decimal(18,4),
    rbm_panel character varying(50),
    unit character varying(50),
    id int NOT NULL
);
