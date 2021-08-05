--
-- Name: bio_asy_analysis_pltfm; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_asy_analysis_pltfm (
    bio_asy_analysis_pltfm_id int NOT NULL,
    platform_name character varying(200),
    platform_version character varying(200),
    platform_description character varying(1000)
);

--
-- Name: bio_assay_analysis_platform_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_asy_analysis_pltfm
    ADD CONSTRAINT bio_assay_analysis_platform_pk PRIMARY KEY (bio_asy_analysis_pltfm_id);

--
-- Name: tf_trg_bio_asy_analysis_pltfm_id(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_bio_asy_analysis_pltfm_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.bio_asy_analysis_pltfm_id is null then
        select nextval('biomart.seq_bio_data_id') into new.bio_asy_analysis_pltfm_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_bio_asy_analysis_pltfm_id; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_bio_asy_analysis_pltfm_id BEFORE INSERT ON bio_asy_analysis_pltfm FOR EACH ROW EXECUTE PROCEDURE tf_trg_bio_asy_analysis_pltfm_id();

