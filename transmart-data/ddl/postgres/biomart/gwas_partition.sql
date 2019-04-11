--
-- Name: gwas_partition; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE gwas_partition (
    bio_asy_analysis_gwas_id int NOT NULL,
    bio_assay_analysis_id int NOT NULL,
    rs_id character varying(50),
    p_value_char character varying(100),
    p_value double precision,
    log_p_value double precision,
    etl_id int,
    ext_data character varying(4000)
);

--
-- Type: REF_CONSTRAINT; Owner: BIOMART; Name: BIO_AA_GWAS_FK
--
ALTER TABLE ONLY gwas_partition
    ADD CONSTRAINT bio_aa_gwas_fk FOREIGN KEY (bio_assay_analysis_id) REFERENCES bio_assay_analysis(bio_assay_analysis_id);
