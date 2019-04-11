--
-- Name: patient_dimension_release; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE patient_dimension_release (
    patient_num int,
    vital_status_cd character varying(50),
    birth_date timestamp,
    death_date timestamp,
    sex_cd character varying(50),
    age_in_years_num int,
    language_cd character varying(50),
    race_cd character varying(100),
    marital_status_cd character varying(50),
    religion_cd character varying(50),
    zip_cd character varying(50),
    statecityzip_path character varying(700),
    update_date timestamp,
    download_date timestamp,
    import_date timestamp,
    sourcesystem_cd character varying(50),
    upload_id int,
    patient_blob text,
    release_study character varying(50)
);

