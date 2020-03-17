--
-- Name: de_subject_proteomics_data; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_subject_proteomics_data (
    trial_name character varying(100),
    protein_annotation_id int,
    component character varying(100),
    patient_id int,
    gene_symbol character varying(100),
    gene_id int,
    assay_id int,
    subject_id character varying(100),
    intensity double precision,
    zscore double precision,
    partition_id int
);

