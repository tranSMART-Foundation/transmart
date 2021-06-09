--
-- Name: de_snp_subject_sorted_def; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_snp_subject_sorted_def (
    snp_subject_sorted_def_id int NOT NULL,
    trial_name character varying(100),
    patient_position int,
    patient_num int,
    subject_id character varying(100)
);

--
-- Name: sys_c0020607; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_snp_subject_sorted_def
    ADD CONSTRAINT sys_c0020607 PRIMARY KEY (snp_subject_sorted_def_id);

--
-- Name: tf_trg_snp_subj_sorted_def_id(); Type: FUNCTION; Schema: deapp; Owner: -
--
CREATE FUNCTION tf_trg_snp_subj_sorted_def_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if coalesce(new.snp_subject_sorted_def_id::text, '') = '' then
	select nextval('deapp.seq_data_id') into new.snp_subject_sorted_def_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_snp_subj_sorted_def_id; Type: TRIGGER; Schema: deapp; Owner: -
--
CREATE TRIGGER trg_snp_subj_sorted_def_id BEFORE INSERT ON de_snp_subject_sorted_def FOR EACH ROW EXECUTE PROCEDURE tf_trg_snp_subj_sorted_def_id();
