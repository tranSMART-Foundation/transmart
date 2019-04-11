--
-- Name: pm_project_data; Type: TABLE; Schema: i2b2pm; Owner: -
--
CREATE TABLE pm_project_data (
    project_id character varying(50) NOT NULL,
    project_name character varying(255),
    project_wiki character varying(255),
    project_key character varying(255),
    project_path character varying(255),
    project_description character varying(2000),
    change_date timestamp,
    entry_date timestamp,
    changeby_char character varying(50),
    status_cd character varying(50)
);
--
-- Name: pm_project_data_pk; Type: CONSTRAINT; Schema: i2b2pm; Owner: -
--
ALTER TABLE ONLY pm_project_data
    ADD CONSTRAINT pm_project_data_pk PRIMARY KEY (project_id);
