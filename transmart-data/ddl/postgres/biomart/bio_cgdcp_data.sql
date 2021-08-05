--
-- Name: bio_cgdcp_data; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_cgdcp_data (
    evidence_code character varying(200),
    negation_indicator character(1),
    cell_line_id int,
    nci_disease_concept_code character varying(200),
    nci_role_code character varying(200),
    nci_drug_concept_code character varying(200),
    bio_data_id int NOT NULL
);

--
-- Name: bio_cgdcp_data_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_cgdcp_data
    ADD CONSTRAINT bio_cgdcp_data_pk PRIMARY KEY (bio_data_id);

