--
-- Name: gse_analysis; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE gse_analysis (
    name character varying(100),
    platform character varying(100),
    test character varying(1000),
    data_ct int,
    fc_mean int,
    fc_stddev int,
    bio_experiment_id int,
    bio_assay_platform_id int,
    bio_assay_analysis_id int,
    analysis1 character varying(300),
    analysis2 character varying(300)
);

