--
-- Name: bio_clinc_trial_pt_group; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_clinc_trial_pt_group (
    bio_experiment_id int NOT NULL,
    bio_clinical_trial_p_group_id int NOT NULL,
    name character varying(510),
    description character varying(1000),
    number_of_patients int,
    patient_group_type_code character varying(200)
);

--
-- Name: bio_clinc_trial_pt_group_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_clinc_trial_pt_group
    ADD CONSTRAINT bio_clinc_trial_pt_group_pk PRIMARY KEY (bio_clinical_trial_p_group_id);

--
-- Name: tf_trg_bio_clin_trl_pt_grp_id(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_bio_clin_trl_pt_grp_id() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
begin
    if new.bio_clinical_trial_p_group_id is null then
        select nextval('biomart.seq_bio_data_id') into new.bio_clinical_trial_p_group_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_bio_clin_trl_pt_grp_id; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_bio_clin_trl_pt_grp_id BEFORE INSERT ON bio_clinc_trial_pt_group FOR EACH ROW EXECUTE PROCEDURE tf_trg_bio_clin_trl_pt_grp_id();

--
-- Name: bio_clinc_trl_pt_grp_exp_fk; Type: FK CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_clinc_trial_pt_group
    ADD CONSTRAINT bio_clinc_trl_pt_grp_exp_fk FOREIGN KEY (bio_experiment_id) REFERENCES bio_clinical_trial(bio_experiment_id);

