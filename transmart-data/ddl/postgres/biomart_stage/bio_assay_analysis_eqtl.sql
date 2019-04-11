--
-- Name: bio_assay_analysis_eqtl; Type: TABLE; Schema: biomart_stage; Owner: -
--
CREATE TABLE bio_assay_analysis_eqtl (
    bio_asy_analysis_eqtl_id int NOT NULL,
    bio_assay_analysis_id int NOT NULL,
    rs_id character varying(50),
    gene character varying(50),
    p_value_char character varying(100),
    cis_trans character varying(10),
    distance_from_gene character varying(10),
    etl_id int,
    ext_data character varying(4000)
);
