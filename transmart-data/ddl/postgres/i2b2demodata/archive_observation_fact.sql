--
-- Name: archive_observation_fact; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE archive_observation_fact (
    encounter_num int NOT NULL,
    patient_num int NOT NULL,
    concept_cd character varying(50) NOT NULL,
    provider_id character varying(50) NOT NULL,
    start_date timestamp NOT NULL,
    modifier_cd character varying(100) default '@' NOT NULL,
    instance_num int default (1) NOT NULL,
    valtype_cd character varying(50),
    tval_char character varying(255),
    nval_num decimal(18,5),
    valueflag_cd character varying(50),
    quantity_num decimal(18,5),
    units_cd character varying(50),
    end_date timestamp,
    location_cd character varying(50),
    observation_blob text,
    confidence_num decimal(18,5),
    update_date timestamp,
    download_date timestamp,
    import_date timestamp,
    sourcesystem_cd character varying(50),
    upload_id int,
    archive_upload_id int
);

--
-- Name: pk_archive_obsfact; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX pk_archive_obsfact ON archive_observation_fact USING btree (encounter_num, patient_num, concept_cd, provider_id, start_date, modifier_cd, archive_upload_id);

