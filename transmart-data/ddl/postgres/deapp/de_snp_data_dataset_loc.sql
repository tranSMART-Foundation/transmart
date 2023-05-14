--
-- Name: de_snp_data_dataset_loc; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_snp_data_dataset_loc (
    snp_data_dataset_loc_id int NOT NULL,
    trial_name character varying(100),
    snp_dataset_id int,
    location int
);

--
-- Name: de_snp_data_dataset_loc_pk; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_snp_data_dataset_loc
    ADD CONSTRAINT de_snp_data_dataset_loc_pk PRIMARY KEY (snp_data_dataset_loc_id);

--
-- Name: tf_trg_snp_data_dataset_loc_id(); Type: FUNCTION; Schema: deapp; Owner: -
--
CREATE FUNCTION tf_trg_snp_data_dataset_loc_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.snp_data_dataset_loc_id is null then
	select nextval('deapp.seq_data_id') into new.snp_data_dataset_loc_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_snp_data_dataset_loc_id; Type: TRIGGER; Schema: deapp; Owner: -
--
CREATE TRIGGER trg_snp_data_dataset_loc_id BEFORE INSERT ON de_snp_data_dataset_loc FOR EACH ROW EXECUTE PROCEDURE tf_trg_snp_data_dataset_loc_id();

--
-- Name: fk_snp_loc_dataset_id; Type: FK CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_snp_data_dataset_loc
    ADD CONSTRAINT fk_snp_loc_dataset_id FOREIGN KEY (snp_dataset_id) REFERENCES de_subject_snp_dataset(subject_snp_dataset_id);

