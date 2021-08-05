--
-- Name: bio_sample; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_sample (
    bio_sample_id int NOT NULL,
    bio_sample_type character varying(100) NOT NULL,
    characteristics character varying(1000),
    source_code character varying(200),
    experiment_id int,
    bio_subject_id int,
    source character varying(200),
    bio_bank_id int,
    bio_patient_event_id int,
    bio_cell_line_id int,
    bio_sample_name character varying(100)
);

--
-- Name: bio_sample_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_sample
    ADD CONSTRAINT bio_sample_pk PRIMARY KEY (bio_sample_id);

--
-- Name: tf_trg_bio_sample_id(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_bio_sample_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.bio_sample_id is null then
        select nextval('biomart.seq_bio_data_id') into new.bio_sample_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_bio_sample_id; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_bio_sample_id BEFORE INSERT ON bio_sample FOR EACH ROW EXECUTE PROCEDURE tf_trg_bio_sample_id();

--
-- Name: bio_sample_bio_subject_fk; Type: FK CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_sample
    ADD CONSTRAINT bio_sample_bio_subject_fk FOREIGN KEY (bio_subject_id) REFERENCES bio_subject(bio_subject_id);

--
-- Name: bio_sample_cl_fk; Type: FK CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_sample
    ADD CONSTRAINT bio_sample_cl_fk FOREIGN KEY (bio_cell_line_id) REFERENCES bio_cell_line(bio_cell_line_id);

--
-- Name: bio_sample_pt_evt_fk; Type: FK CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_sample
    ADD CONSTRAINT bio_sample_pt_evt_fk FOREIGN KEY (bio_patient_event_id) REFERENCES bio_patient_event(bio_patient_event_id);

