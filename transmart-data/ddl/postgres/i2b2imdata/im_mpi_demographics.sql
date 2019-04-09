--
-- Name: im_mpi_demographics; Type: TABLE; Schema: i2b2imdata; Owner: -
--
CREATE TABLE im_mpi_demographics (
    global_id character varying(200) NOT NULL,
    global_status character varying(50),
    demographics character varying(400),
    update_date timestamp without time zone,
    download_date timestamp without time zone,
    import_date timestamp without time zone,
    sourcesystem_cd character varying(50),
    upload_id numeric(38,0)
);
--
-- Name: im_mpi_demographics_pk; Type: CONSTRAINT; Schema: i2b2imdata; Owner: -
--
ALTER TABLE ONLY im_mpi_demographics
    ADD CONSTRAINT im_mpi_demographics_pk PRIMARY KEY (global_id);
