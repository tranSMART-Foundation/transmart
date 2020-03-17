--
-- Name: tmp_subject_rbm_logs; Type: TABLE; Schema: tm_wz; Owner: -
--
CREATE TABLE tmp_subject_rbm_logs (
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
    value decimal(18,4),
    log_intensity double precision
);
