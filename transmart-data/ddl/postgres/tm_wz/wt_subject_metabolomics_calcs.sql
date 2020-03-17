--
-- Name: wt_subject_metabolomics_calcs; Type: TABLE; Schema: tm_wz; Owner: -
--
CREATE TABLE wt_subject_metabolomics_calcs (
    trial_name character varying(100),
    probeset character varying(500),
    mean_intensity double precision,
    median_intensity double precision,
    stddev_intensity double precision
);

