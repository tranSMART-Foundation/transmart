--
-- Name: wt_subject_mrna_data; Type: TABLE; Schema: tm_wz; Owner: -
--
CREATE TABLE wt_subject_mrna_data (
    probeset character varying(500),
    expr_id character varying(500),
    intensity_value double precision,
    assay_id int,
    patient_id int,
    sample_id int,
    subject_id character varying(100),
    trial_name character varying(100),
    timepoint character varying(250),
    sample_type character varying(100),
    platform character varying(200),
    tissue_type character varying(200)
);

