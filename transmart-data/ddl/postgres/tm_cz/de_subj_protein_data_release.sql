--
-- Name: de_subj_protein_data_release; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE de_subj_protein_data_release (
    trial_name character varying(100),
    component character varying(15),
    intensity double precision,
    patient_id int,
    subject_id character varying(100),
    gene_symbol character varying(100),
    gene_id int,
    assay_id int,
    timepoint character varying(250),
    n_value int,
    mean_intensity double precision,
    stddev_intensity double precision,
    median_intensity double precision,
    zscore double precision,
    release_study character varying(15)
);

