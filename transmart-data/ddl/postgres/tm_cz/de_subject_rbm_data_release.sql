--
-- Name: de_subject_rbm_data_release; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE de_subject_rbm_data_release (
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
    value int,
    log_intensity double precision,
    mean_intensity double precision,
    stddev_intensity double precision,
    median_intensity double precision,
    zscore double precision,
    rbm_panel character varying(50),
    release_study character varying(15)
);

