--
-- Name: application_settings; Type: TABLE; Schema: biomart_user; Owner: -
--
CREATE TABLE application_settings (
    id int NOT NULL,
    version int NOT NULL,
    fieldname character varying(255) NOT NULL,
    fieldvalue character varying(2000) NOT NULL,
    last_updated timestamp NOT NULL,
    userid int NOT NULL
);

--
-- Name: application_settings_pk; Type: CONSTRAINT; Schema: biomart_user; Owner: -
--
ALTER TABLE ONLY application_settings
    ADD CONSTRAINT application_settings_pk PRIMARY KEY (id);
