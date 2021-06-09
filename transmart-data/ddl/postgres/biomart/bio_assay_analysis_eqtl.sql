--
-- Name: bio_assay_analysis_eqtl; Type: TABLE; Schema: biomart; Owner: -
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
    ext_data character varying(4000),
    p_value double precision,
    log_p_value double precision
);

--
-- Name: bio_assay_analysis_eqtl_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_assay_analysis_eqtl
    ADD CONSTRAINT bio_assay_analysis_eqtl_pk PRIMARY KEY (bio_asy_analysis_eqtl_id);

--
-- Name: tf_trg_bio_asy_analysis_eqtl_id(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_bio_asy_analysis_eqtl_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.bio_asy_analysis_eqtl_id is null then
        select nextval('biomart.seq_bio_data_id') into new.bio_asy_analysis_eqtl_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_bio_asy_analysis_eqtl_id; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_bio_asy_analysis_eqtl_id BEFORE INSERT ON bio_assay_analysis_eqtl FOR EACH ROW EXECUTE PROCEDURE tf_trg_bio_asy_analysis_eqtl_id();
