--
-- Name: bio_experiment_release; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE bio_experiment_release (
    bio_experiment_id int,
    bio_experiment_type character varying(200),
    title character varying(1000),
    description character varying(2000),
    design character varying(2000),
    start_date timestamp,
    completion_date timestamp,
    primary_investigator character varying(400),
    contact_field character varying(400),
    etl_id character varying(100),
    status character varying(100),
    overall_design character varying(2000),
    accession character varying(100) NOT NULL,
    entrydt timestamp,
    updated timestamp,
    institution character varying(100),
    country character varying(50),
    biomarker_type character varying(255),
    target character varying(255),
    access_type character varying(100),
    release_study character varying(100) NOT NULL
);

