--
-- Name: bio_content_reference; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_content_reference (
    bio_content_reference_id int NOT NULL,
    bio_content_id int NOT NULL,
    bio_data_id int NOT NULL,
    content_reference_type character varying(200) NOT NULL,
    etl_id int,
    etl_id_c character varying(30)
);

--
-- Name: bio_content_reference_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_content_reference
    ADD CONSTRAINT bio_content_reference_pk PRIMARY KEY (bio_content_reference_id);

--
-- Name: tf_trg_bio_content_ref_id(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_bio_content_ref_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.bio_content_reference_id is null then
        select nextval('biomart.seq_bio_data_id') into new.bio_content_reference_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_bio_content_ref_id; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_bio_content_ref_id BEFORE INSERT ON bio_content_reference FOR EACH ROW EXECUTE PROCEDURE tf_trg_bio_content_ref_id();

--
-- Name: bio_content_ref_cont_fk; Type: FK CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_content_reference
    ADD CONSTRAINT bio_content_ref_cont_fk FOREIGN KEY (bio_content_id) REFERENCES bio_content(bio_file_content_id);

