--
-- Name: wt_subject_mirna_calcs; Type: TABLE; Schema: tm_wz; Owner: -
--
CREATE TABLE wt_subject_mirna_calcs (
    trial_name character varying(50),
    probeset_id character varying(1000),
    mean_intensity double precision,
    median_intensity double precision,
    stddev_intensity double precision
);

