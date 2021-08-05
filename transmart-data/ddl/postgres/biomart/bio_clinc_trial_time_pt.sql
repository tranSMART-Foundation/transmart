--
-- Name: bio_clinc_trial_time_pt; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_clinc_trial_time_pt (
    bio_clinc_trial_tm_pt_id int NOT NULL,
    time_point character varying(200),
    time_point_code character varying(200),
    start_date timestamp,
    end_date timestamp,
    bio_experiment_id int NOT NULL
);

--
-- Name: bio_clinc_trial_time_pt_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_clinc_trial_time_pt
    ADD CONSTRAINT bio_clinc_trial_time_pt_pk PRIMARY KEY (bio_clinc_trial_tm_pt_id);

--
-- Name: tf_trg_bio_cl_trl_time_pt_id(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_bio_cl_trl_time_pt_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.bio_clinc_trial_tm_pt_id is null then
        select nextval('biomart.seq_bio_data_id') into new.bio_clinc_trial_tm_pt_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_bio_cl_trl_time_pt_id; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_bio_cl_trl_time_pt_id BEFORE INSERT ON bio_clinc_trial_time_pt FOR EACH ROW EXECUTE PROCEDURE tf_trg_bio_cl_trl_time_pt_id();

--
-- Name: bio_cli_trial_time_trl_fk; Type: FK CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_clinc_trial_time_pt
    ADD CONSTRAINT bio_cli_trial_time_trl_fk FOREIGN KEY (bio_experiment_id) REFERENCES bio_clinical_trial(bio_experiment_id);

