--
-- Name: bio_assay_data; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_assay_data (
    bio_sample_id int,
    bio_assay_data_id int NOT NULL,
    log2_value double precision,
    log10_value double precision,
    numeric_value int,
    text_value character varying(200),
    float_value double precision,
    feature_group_name character varying(100) NOT NULL,
    bio_experiment_id int,
    bio_assay_dataset_id int,
    bio_assay_id int,
    etl_id int
);

--
-- Name: bio_experiment_data_fact_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_assay_data
    ADD CONSTRAINT bio_experiment_data_fact_pk PRIMARY KEY (bio_assay_data_id);

--
-- Name: tf_trg_bio_assay_data_id(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_bio_assay_data_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.bio_assay_data_id is null then
        select nextval('biomart.seq_bio_data_id') into new.bio_assay_data_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_bio_assay_data_id; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_bio_assay_data_id BEFORE INSERT ON bio_assay_data FOR EACH ROW EXECUTE PROCEDURE tf_trg_bio_assay_data_id();

--
-- Name: bio_asy_dt_ds_fk; Type: FK CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_assay_data
    ADD CONSTRAINT bio_asy_dt_ds_fk FOREIGN KEY (bio_assay_dataset_id) REFERENCES bio_assay_dataset(bio_assay_dataset_id);

--
-- Name: bio_asy_exp_fk; Type: FK CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_assay_data
    ADD CONSTRAINT bio_asy_exp_fk FOREIGN KEY (bio_experiment_id) REFERENCES bio_experiment(bio_experiment_id);

--
-- Name: bio_exp_data_fact_samp_fk; Type: FK CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_assay_data
    ADD CONSTRAINT bio_exp_data_fact_samp_fk FOREIGN KEY (bio_sample_id) REFERENCES bio_sample(bio_sample_id);

