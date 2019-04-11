--
-- Name: im_project_patients; Type: TABLE; Schema: i2b2imdata; Owner: -
--
CREATE TABLE im_project_patients (
    project_id character varying(50) NOT NULL,
    global_id character varying(200) NOT NULL,
    patient_project_status character varying(50),
    update_date timestamp,
    download_date timestamp,
    import_date timestamp,
    sourcesystem_cd character varying(50),
    upload_id int
);
--
-- Name: im_project_patients_pk; Type: CONSTRAINT; Schema: i2b2imdata; Owner: -
--
ALTER TABLE ONLY im_project_patients
    ADD CONSTRAINT im_project_patients_pk PRIMARY KEY (project_id,global_id);
