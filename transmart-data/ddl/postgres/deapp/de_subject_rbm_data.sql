--
-- Name: de_subject_rbm_data; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_subject_rbm_data (
    trial_name character varying(100),
    antigen_name character varying(100),
    n_value int,
    patient_id int,
    gene_symbol character varying(100),
    gene_id integer,
    assay_id int,
    normalized_value double precision,
    concept_cd character varying(100),
    timepoint character varying(250),
    data_uid character varying(100),
    value double precision,
    log_intensity double precision,
    mean_intensity double precision,
    stddev_intensity double precision,
    median_intensity double precision,
    zscore double precision,
    rbm_panel character varying(50),
    unit character varying(50),
    id int NOT NULL,
    partition_id int
);

--
-- Name: pk_de_subject_rbm_data; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_subject_rbm_data
    ADD CONSTRAINT pk_de_subject_rbm_data PRIMARY KEY (id);

--
-- Name: de_subject_rbm_mcidx1; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_subject_rbm_mcidx1 ON de_subject_rbm_data USING btree (trial_name);

--
-- Name: de_subject_rbm_mcidx2; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_subject_rbm_mcidx2 ON de_subject_rbm_data USING btree (antigen_name);

--
-- Name: de_subject_rbm_mcidx3; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_subject_rbm_mcidx3 ON de_subject_rbm_data USING btree (patient_id);

--
-- Name: de_subject_rbm_mcidx4; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_subject_rbm_mcidx4 ON de_subject_rbm_data USING btree (gene_symbol);

--
-- Name: tf_trg_de_subj_rbm_data_id(); Type: FUNCTION; Schema: deapp; Owner: -
--
CREATE FUNCTION tf_trg_de_subj_rbm_data_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.id is null then
	select nextval('deapp.de_subject_rbm_data_seq') into new.id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_de_subj_rbm_data_id; Type: TRIGGER; Schema: deapp; Owner: -
--
CREATE TRIGGER trg_de_subj_rbm_data_id BEFORE INSERT ON de_subject_rbm_data FOR EACH ROW EXECUTE PROCEDURE tf_trg_de_subj_rbm_data_id();

--
-- Name: de_subject_rbm_data_seq; Type: SEQUENCE; Schema: deapp; Owner: -
--
CREATE SEQUENCE de_subject_rbm_data_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

