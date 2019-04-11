--
-- Name: cms_section; Type: TABLE; Schema: biomart_user; Owner: -
--
CREATE TABLE cms_section (
    id int NOT NULL,
    name character varying(255) NOT NULL,
    instance_type character varying(255) NOT NULL,
    version character varying(255) NOT NULL,
    closure character varying(2000) NOT NULL
);

--
-- Name: cms_section_pk; Type: CONSTRAINT; Schema: biomart_user; Owner: -
--
ALTER TABLE ONLY cms_section
    ADD CONSTRAINT cms_section_pk PRIMARY KEY (id);
