--
-- Name: tmp_analysis_gwas_top500; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE tmp_analysis_gwas_top500 (
    bio_asy_analysis_gwas_id int NOT NULL,
    bio_assay_analysis_id int NOT NULL,
    rs_id character varying(50),
    p_value double precision,
    log_p_value double precision,
    etl_id int,
    ext_data character varying(4000),
    p_value_char character varying(100),
    rnum int
);
