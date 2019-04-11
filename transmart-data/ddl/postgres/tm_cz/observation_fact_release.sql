--
-- Name: observation_fact_release; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE observation_fact_release (
    encounter_num int,
    patient_num int,
    concept_cd character varying(50) NOT NULL,
    provider_id character varying(50) NOT NULL,
    start_date timestamp,
    modifier_cd character varying(100),
    valtype_cd character varying(50),
    tval_char character varying(255),
    nval_num double precision,
    valueflag_cd character varying(50),
    quantity_num double precision,
    units_cd character varying(50),
    end_date timestamp,
    location_cd character varying(50) NOT NULL,
    confidence_num int,
    update_date timestamp,
    download_date timestamp,
    import_date timestamp,
    sourcesystem_cd character varying(50),
    upload_id int,
    observation_blob text,
    release_study character varying(100)
);

