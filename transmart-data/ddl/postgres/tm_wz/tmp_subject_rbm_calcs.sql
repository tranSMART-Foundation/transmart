--
-- Name: tmp_subject_rbm_calcs; Type: TABLE; Schema: tm_wz; Owner: -
--
CREATE TABLE tmp_subject_rbm_calcs (
    trial_name character varying(100),
    gene_symbol character varying(100),
    antigen_name character varying(100),
    mean_intensity double precision,
    median_intensity double precision,
    stddev_intensity double precision
);
