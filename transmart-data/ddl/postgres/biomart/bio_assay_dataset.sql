--
-- Name: bio_assay_dataset; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_assay_dataset (
    bio_assay_dataset_id int NOT NULL,
    dataset_name character varying(400),
    dataset_description character varying(1000),
    dataset_criteria character varying(1000),
    create_date timestamp,
    bio_experiment_id int NOT NULL,
    bio_assay_id int,
    etl_id character varying(100),
    accession character varying(50)
);

--
-- Name: bio_dataset_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_assay_dataset
    ADD CONSTRAINT bio_dataset_pk PRIMARY KEY (bio_assay_dataset_id);

--
-- Name: tf_trg_bio_assay_dataset_id(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_bio_assay_dataset_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.bio_assay_dataset_id is null then
        select nextval('biomart.seq_bio_data_id') into new.bio_assay_dataset_id ;
    end if;
    return new;
end;

$$;

--
-- Name: trg_bio_assay_dataset_id; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_bio_assay_dataset_id BEFORE INSERT ON bio_assay_dataset FOR EACH ROW EXECUTE PROCEDURE tf_trg_bio_assay_dataset_id();

--
-- Name: bio_dataset_experiment_fk; Type: FK CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_assay_dataset
    ADD CONSTRAINT bio_dataset_experiment_fk FOREIGN KEY (bio_experiment_id) REFERENCES bio_experiment(bio_experiment_id);

