--
-- Name: bio_clinc_trial_attr; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_clinc_trial_attr (
    bio_clinc_trial_attr_id int NOT NULL,
    property_code character varying(200) NOT NULL,
    property_value character varying(200),
    bio_experiment_id int NOT NULL
);

--
-- Name: bio_clinc_trial_attr_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_clinc_trial_attr
    ADD CONSTRAINT bio_clinc_trial_attr_pk PRIMARY KEY (bio_clinc_trial_attr_id);

--
-- Name: tf_trg_bio_cln_trl_attr_id(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_bio_cln_trl_attr_id() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
begin
    if new.bio_clinc_trial_attr_id is null then
        select nextval('biomart.seq_bio_data_id') into new.bio_clinc_trial_attr_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_bio_cln_trl_attr_id; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_bio_cln_trl_attr_id BEFORE INSERT ON bio_clinc_trial_attr FOR EACH ROW EXECUTE PROCEDURE tf_trg_bio_cln_trl_attr_id();

--
-- Name: bio_clinical_trial_property_bi; Type: FK CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_clinc_trial_attr
    ADD CONSTRAINT bio_clinical_trial_property_bi FOREIGN KEY (bio_experiment_id) REFERENCES bio_clinical_trial(bio_experiment_id);

