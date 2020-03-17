--
-- Name: stg_subject_rbm_data; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE stg_subject_rbm_data (
    trial_name character varying(100),
    antigen_name character varying(100),
    value_text character varying(100),
    value_number int,
    timepoint character varying(250),
    assay_id character varying(100),
    sample_id character varying(100),
    subject_id character varying(100),
    site_id character varying(100)
);
