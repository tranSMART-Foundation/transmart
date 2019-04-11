--
-- Name: im_project_sites; Type: TABLE; Schema: i2b2imdata; Owner: -
--
CREATE TABLE im_project_sites (
    project_id character varying(50) NOT NULL,
    lcl_site character varying(50) NOT NULL,
    project_status character varying(50),
    update_date timestamp,
    download_date timestamp,
    import_date timestamp,
    sourcesystem_cd character varying(50),
    upload_id int
);
--
-- Name: im_project_sites_pk; Type: CONSTRAINT; Schema: i2b2imdata; Owner: -
--
ALTER TABLE ONLY im_project_sites
    ADD CONSTRAINT im_project_sites_pk PRIMARY KEY (project_id,lcl_site);
