--
-- Name: bio_assay_analysis_gwas; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_assay_analysis_gwas (
    bio_asy_analysis_gwas_id int NOT NULL,
    bio_assay_analysis_id int NOT NULL,
    rs_id character varying(50),
    p_value_char character varying(100),
    etl_id int,
    ext_data character varying(4000),
    p_value double precision,
    log_p_value double precision,
    effect_allele character varying(100),
    other_allele character varying(100),
    beta character varying(100),
    standard_error character varying(100)
);

--
-- Name: bio_asy_analysis_gwas_id; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_assay_analysis_gwas
    ADD CONSTRAINT bio_asy_analysis_gwas_id PRIMARY KEY (bio_asy_analysis_gwas_id);


