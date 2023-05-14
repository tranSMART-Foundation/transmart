--
-- Name: de_snp_data_by_patient; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_snp_data_by_patient (
    snp_data_by_patient_id int NOT NULL,
    snp_dataset_id int,
    trial_name character varying(100),
    patient_num int,
    chrom character varying(16),
    data_by_patient_chr text,
    ped_by_patient_chr text
);

--
-- Name: de_snp_data_by_patient_pk; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_snp_data_by_patient
    ADD CONSTRAINT de_snp_data_by_patient_pk PRIMARY KEY (snp_data_by_patient_id);

--
-- Name: tf_trg_snp_data_by_patient_id(); Type: FUNCTION; Schema: deapp; Owner: -
--
CREATE FUNCTION tf_trg_snp_data_by_patient_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.snp_data_by_patient_id is null then
        select nextval('deapp.seq_data_id') into new.snp_data_by_patient_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_snp_data_by_patient_id; Type: TRIGGER; Schema: deapp; Owner: -
--
CREATE TRIGGER trg_snp_data_by_patient_id BEFORE INSERT ON de_snp_data_by_patient FOR EACH ROW EXECUTE PROCEDURE tf_trg_snp_data_by_patient_id();

--
-- Name: fk_snp_dataset_id; Type: FK CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_snp_data_by_patient
    ADD CONSTRAINT fk_snp_dataset_id FOREIGN KEY (snp_dataset_id) REFERENCES de_subject_snp_dataset(subject_snp_dataset_id);

