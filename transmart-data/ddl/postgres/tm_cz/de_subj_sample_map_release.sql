--
-- Name: de_subj_sample_map_release; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE de_subj_sample_map_release (
    patient_id int,
    site_id character varying(100),
    subject_id character varying(100),
    subject_type character varying(100),
    concept_code character varying(1000),
    assay_id int,
    patient_uid character varying(50),
    sample_type character varying(100),
    assay_uid character varying(100),
    trial_name character varying(100),
    timepoint character varying(250),
    timepoint_cd character varying(50),
    sample_type_cd character varying(50),
    tissue_type_cd character varying(50),
    platform character varying(50),
    platform_cd character varying(50),
    tissue_type character varying(100),
    data_uid character varying(100),
    gpl_id character varying(50),
    rbm_panel character varying(50),
    sample_id int,
    sample_cd character varying(200),
    category_cd character varying(1000),
    release_study character varying(30)
);

