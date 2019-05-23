--
-- Name: bio_analysis_attribute; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_analysis_attribute (
    study_id character varying(255),
    bio_assay_analysis_id int NOT NULL,
    term_id int,
    source_cd character varying(255),
    bio_analysis_attribute_id int NOT NULL
);

--
-- Name: pk_baa_id; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_analysis_attribute
    ADD CONSTRAINT pk_baa_id PRIMARY KEY (bio_analysis_attribute_id);

--
-- Name: tf_trg_bio_analysis_attribute_id(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_bio_analysis_attribute_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.bio_analysis_attribute_id is null then
	select nextval('biomart.seq_bio_data_id') into new.bio_analysis_attribute_id;
    end if;
    return new;
end;
$$;

--
-- Name: trg_bio_analysis_attribute_id; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_bio_analysis_attribute_id BEFORE INSERT ON bio_analysis_attribute FOR EACH ROW EXECUTE PROCEDURE tf_trg_bio_analysis_attribute_id();

