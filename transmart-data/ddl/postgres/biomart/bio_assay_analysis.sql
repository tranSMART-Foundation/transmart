--
-- Name: bio_assay_analysis; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_assay_analysis (
    analysis_name character varying(500),
    short_description character varying(510),
    analysis_create_date timestamp,
    analyst_id character varying(510),
    bio_assay_analysis_id int NOT NULL,
    analysis_version character varying(200),
    fold_change_cutoff double precision,
    pvalue_cutoff double precision,
    rvalue_cutoff double precision,
    bio_asy_analysis_pltfm_id int,
    bio_source_import_id int,
    analysis_type character varying(200),
    analyst_name character varying(250),
    analysis_method_cd character varying(50),
    bio_assay_data_type character varying(50),
    etl_id character varying(100),
    long_description character varying(4000),
    qa_criteria character varying(4000),
    data_count int,
    tea_data_count int,
    analysis_update_date date,
    lsmean_cutoff double precision,
    etl_id_source int
);

--
-- Name: bio_assay_anl_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_assay_analysis
    ADD CONSTRAINT bio_assay_anl_pk PRIMARY KEY (bio_assay_analysis_id);

--
-- Name: tf_trg_bio_assay_analysis_id(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_bio_assay_analysis_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.bio_assay_analysis_id is null then
        select nextval('biomart.seq_bio_data_id') into new.bio_assay_analysis_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_bio_assay_analysis_id; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_bio_assay_analysis_id BEFORE INSERT ON bio_assay_analysis FOR EACH ROW EXECUTE PROCEDURE tf_trg_bio_assay_analysis_id();

--
-- Name: tf_trg_bio_assay_analysis_uid(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_bio_assay_analysis_uid() RETURNS trigger
    LANGUAGE plpgsql
AS $$
    declare
    rec_count int;
begin
    select count(*) into rec_count 
      from biomart.bio_data_uid 
     where bio_data_id = new.bio_assay_analysis_id;
    
    if rec_count = 0 then
	insert into biomart.bio_data_uid (bio_data_id, unique_id, bio_data_type)
	values (new.bio_assay_analysis_id, biomart.bio_assay_analysis_uid(new.bio_assay_analysis_id::text), 'BIO_ASSAY_ANALYSIS');
    end if;
    return new;
end;
$$;

--
-- Name: trg_bio_assay_analysis_uid; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_bio_assay_analysis_uid BEFORE INSERT ON bio_assay_analysis FOR EACH ROW EXECUTE PROCEDURE tf_trg_bio_assay_analysis_uid();

--
-- Name: bio_assay_ans_pltfm_fk; Type: FK CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_assay_analysis
    ADD CONSTRAINT bio_assay_ans_pltfm_fk FOREIGN KEY (bio_asy_analysis_pltfm_id) REFERENCES bio_asy_analysis_pltfm(bio_asy_analysis_pltfm_id);

