--
-- Name: search_role; Type: TABLE; Schema: searchapp; Owner: -
--
CREATE TABLE search_role (
    id int NOT NULL,
    version int,
    authority character varying(255),
    description character varying(255)
);

--
-- Name: search_role_pk; Type: CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY search_role
    ADD CONSTRAINT search_role_pk PRIMARY KEY (id);

