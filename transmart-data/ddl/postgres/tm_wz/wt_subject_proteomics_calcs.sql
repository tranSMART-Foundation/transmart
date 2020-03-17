--
-- Name: wt_subject_proteomics_calcs; Type: TABLE; Schema: tm_wz; Owner: -
--
CREATE TABLE wt_subject_proteomics_calcs (
    trial_name character varying(100),
    probeset_id character varying(500),
    mean_intensity double precision,
    median_intensity double precision,
    stddev_intensity double precision
);

