--
-- Name: concept_id; Type: SEQUENCE; Schema: i2b2demodata; Owner: -
--
CREATE SEQUENCE concept_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: concept_dimension; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE concept_dimension (
    concept_path character varying(700) NOT NULL,
    concept_cd character varying(50),
    name_char character varying(2000),
    concept_blob text,
    update_date timestamp,
    download_date timestamp,
    import_date timestamp,
    sourcesystem_cd character varying(50),
    upload_id int
);

--
-- name: concept_dimension_pk; Type: CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY concept_dimension
    ADD CONSTRAINT concept_dimension_pk PRIMARY KEY (concept_path);

--
-- Name: cd_uploadid_idx; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX cd_uploadid_idx ON concept_dimension USING btree (upload_id);

--
-- Name: cd_conceptcd_idx; Type: INDEX; Schema: i2b2demodata; Owner: -
--
-- not in i2b2
CREATE INDEX cd_conceptcd_idx ON concept_dimension USING btree (concept_cd);

--
-- Name: cd_path_cd_idx; Type: INDEX; Schema: i2b2demodata; Owner: -
--
-- not in i2b2
CREATE INDEX cd_path_cd_idx ON concept_dimension USING btree (concept_path,concept_cd);

--
-- Name: tf_trg_concept_dimension_cd(); Type: FUNCTION; Schema: i2b2demodata; Owner: -
--
CREATE FUNCTION tf_trg_concept_dimension_cd() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.concept_cd is null then
        select nextval('i2b2demodata.concept_id') into new.concept_cd ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_concept_dimension_cd; Type: TRIGGER; Schema: i2b2demodata; Owner: -
--
CREATE TRIGGER trg_concept_dimension_cd BEFORE INSERT ON concept_dimension FOR EACH ROW EXECUTE PROCEDURE tf_trg_concept_dimension_cd();
