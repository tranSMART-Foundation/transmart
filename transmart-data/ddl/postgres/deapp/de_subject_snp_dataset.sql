--
-- Name: de_subject_snp_dataset; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_subject_snp_dataset (
    subject_snp_dataset_id int NOT NULL,
    dataset_name character varying(255),
    concept_cd character varying(255),
    platform_name character varying(255),
    trial_name character varying(255),
    patient_num int,
    timepoint character varying(255),
    subject_id character varying(255),
    sample_type character varying(255),
    paired_dataset_id int,
    patient_gender character varying(1)
);

--
-- Name: sys_c0020606; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_subject_snp_dataset
    ADD CONSTRAINT sys_c0020606 PRIMARY KEY (subject_snp_dataset_id);

--
-- Name: tf_trg_de_subject_snp_dataset_id(); Type: FUNCTION; Schema: deapp; Owner: -
--
CREATE FUNCTION tf_trg_de_subject_snp_dataset_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.subject_snp_dataset_id is null then
        select nextval('deapp.seq_data_id') into new.subject_snp_dataset_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_de_subject_snp_dataset_id; Type: TRIGGER; Schema: deapp; Owner: -
--
CREATE TRIGGER trg_de_subject_snp_dataset_id BEFORE INSERT ON de_subject_snp_dataset FOR EACH ROW EXECUTE PROCEDURE tf_trg_de_subject_snp_dataset_id();

