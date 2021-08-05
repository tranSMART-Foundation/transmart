--
-- Name: bio_cell_line; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_cell_line (
    disease character varying(510),
    primary_site character varying(510),
    metastatic_site character varying(510),
    species character varying(510),
    attc_number character varying(510),
    cell_line_name character varying(510),
    bio_cell_line_id int NOT NULL,
    bio_disease_id int,
    origin character varying(200),
    description character varying(500),
    disease_stage character varying(100),
    disease_subtype character varying(200),
    etl_reference_link character varying(300)
);

--
-- Name: celllinedictionary_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_cell_line
    ADD CONSTRAINT celllinedictionary_pk PRIMARY KEY (bio_cell_line_id);

--
-- Name: bio_cell_line_name_idx; Type: INDEX; Schema: biomart; Owner: -
--
CREATE INDEX bio_cell_line_name_idx ON bio_cell_line USING btree (cell_line_name, bio_cell_line_id);

--
-- Name: tf_trg_bio_cell_line_id(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_bio_cell_line_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.bio_cell_line_id is null then
        select nextval('biomart.seq_bio_data_id') into new.bio_cell_line_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_bio_cell_line_id; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_bio_cell_line_id BEFORE INSERT ON bio_cell_line FOR EACH ROW EXECUTE PROCEDURE tf_trg_bio_cell_line_id();

--
-- Name: cd_disease_fk; Type: FK CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_cell_line
    ADD CONSTRAINT cd_disease_fk FOREIGN KEY (bio_disease_id) REFERENCES bio_disease(bio_disease_id);

