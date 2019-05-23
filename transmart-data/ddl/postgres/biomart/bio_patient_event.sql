--
-- Name: bio_patient_event; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_patient_event (
    bio_patient_event_id int NOT NULL,
    bio_patient_id int NOT NULL,
    event_code character varying(200),
    event_type_code character varying(200),
    event_date timestamp,
    site character varying(400),
    bio_clinic_trial_timepoint_id int NOT NULL
);

--
-- Name: bio_patient_event_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_patient_event
    ADD CONSTRAINT bio_patient_event_pk PRIMARY KEY (bio_patient_event_id);

--
-- Name: tf_trg_bio_patient_event_id(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_bio_patient_event_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.bio_patient_event_id is null then
        select nextval('biomart.seq_bio_data_id') into new.bio_patient_event_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_bio_patient_event_id; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_bio_patient_event_id BEFORE INSERT ON bio_patient_event FOR EACH ROW EXECUTE PROCEDURE tf_trg_bio_patient_event_id();

--
-- Name: bio_pt_event_bio_pt_fk; Type: FK CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_patient_event
    ADD CONSTRAINT bio_pt_event_bio_pt_fk FOREIGN KEY (bio_patient_id) REFERENCES bio_patient(bio_patient_id);

--
-- Name: bio_pt_event_bio_trl_tp_fk; Type: FK CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_patient_event
    ADD CONSTRAINT bio_pt_event_bio_trl_tp_fk FOREIGN KEY (bio_clinic_trial_timepoint_id) REFERENCES bio_clinc_trial_time_pt(bio_clinc_trial_tm_pt_id);

