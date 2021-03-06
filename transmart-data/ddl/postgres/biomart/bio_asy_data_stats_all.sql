--
-- Name: bio_asy_data_stats_all; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_asy_data_stats_all (
    bio_assay_data_stats_id int NOT NULL,
    bio_sample_count int,
    quartile_1 double precision,
    quartile_2 double precision,
    quartile_3 double precision,
    max_value double precision,
    min_value double precision,
    bio_sample_id int,
    feature_group_name character varying(120),
    value_normalize_method character varying(50),
    bio_experiment_id int,
    mean_value double precision,
    std_dev_value double precision,
    bio_assay_dataset_id int
);

--
-- Name: bio_asy_dt_stats_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_asy_data_stats_all
    ADD CONSTRAINT bio_asy_dt_stats_pk PRIMARY KEY (bio_assay_data_stats_id);

--
-- Name: bio_a__d_s_ds_idx; Type: INDEX; Schema: biomart; Owner: -
--
CREATE INDEX bio_a__d_s_ds_idx ON bio_asy_data_stats_all USING btree (bio_assay_dataset_id);

--
-- Name: bio_a__d_s_exp_idx; Type: INDEX; Schema: biomart; Owner: -
--
CREATE INDEX bio_a__d_s_exp_idx ON bio_asy_data_stats_all USING btree (bio_experiment_id);

--
-- Name: bio_a__d_s_f_g_idx; Type: INDEX; Schema: biomart; Owner: -
--
CREATE INDEX bio_a__d_s_f_g_idx ON bio_asy_data_stats_all USING btree (feature_group_name);

--
-- Name: tf_trg_bio_asy_dt_stats_id(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_bio_asy_dt_stats_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.bio_assay_data_stats_id is null
    then
	select nextval('biomart.seq_bio_data_fact_id') into new.bio_assay_data_stats_id;
	end if;
    return new;
end;
$$;

--
-- Name: trg_bio_asy_dt_stats_id; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_bio_asy_dt_stats_id BEFORE INSERT ON bio_asy_data_stats_all FOR EACH ROW EXECUTE PROCEDURE tf_trg_bio_asy_dt_stats_id();

--
-- Name: bio_asy_dt_stats_smp_fk; Type: FK CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_asy_data_stats_all
    ADD CONSTRAINT bio_asy_dt_stats_smp_fk FOREIGN KEY (bio_sample_id) REFERENCES bio_sample(bio_sample_id);

--
-- Name: seq_bio_data_fact_id; Type: SEQUENCE; Schema: biomart; Owner: -
--
CREATE SEQUENCE seq_bio_data_fact_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 20;

