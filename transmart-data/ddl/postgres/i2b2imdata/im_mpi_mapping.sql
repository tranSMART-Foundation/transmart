--
-- Name: im_mpi_mapping; Type: TABLE; Schema: i2b2imdata; Owner: -
--
CREATE TABLE im_mpi_mapping (
    global_id character varying(200) NOT NULL,
    lcl_site character varying(50) NOT NULL,
    lcl_id character varying(200) NOT NULL,
    lcl_status character varying(50),
    update_date timestamp without time zone NOT NULL,
    download_date timestamp without time zone,
    import_date timestamp without time zone,
    sourcesystem_cd character varying(50),
    upload_id numeric(38,0)
);
--
-- Name: im_mpi_mapping_pk; Type: CONSTRAINT; Schema: i2b2imdata; Owner: -
--
ALTER TABLE ONLY im_mpi_mapping
    ADD CONSTRAINT im_mpi_mapping_pk PRIMARY KEY (lcl_site,lcl_id,update_date);
