--
-- Name: import_xnat_configuration; Type: TABLE; Schema: searchapp; Owner: -
--
CREATE TABLE searchapp.import_xnat_configuration (
    id bigint NOT NULL,
    version bigint NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    url character varying(255) NOT NULL,
    username character varying(255) NOT NULL,
    project character varying(255) NOT NULL,
    node character varying(255) NOT NULL
);
ALTER TABLE searchapp.import_xnat_configuration OWNER TO searchapp;
GRANT ALL ON TABLE searchapp.import_xnat_configuration TO searchapp;

--
-- Name: pk_import_xnat_configuration; Type: CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY searchapp.import_xnat_configuration
    ADD CONSTRAINT pk_import_xnat_configuration PRIMARY KEY (id)
