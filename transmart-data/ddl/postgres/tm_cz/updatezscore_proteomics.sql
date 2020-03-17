--
-- Name: updatezscore_proteomics; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE updatezscore_proteomics (
    trial_name character varying(100),
    protein_annotation_id int,
    component character varying(100),
    patient_id int,
    gene_symbol character varying(100),
    gene_id character varying(100),
    assay_id int,
    subject_id character varying(100),
    intensity double precision,
    zscore double precision,
    log_intensity double precision,
    mean_value double precision,
    median_value double precision,
    stddev_value double precision,
    timepoint character varying(250)
);
