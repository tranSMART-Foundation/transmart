--
-- Name: de_qpcr_mirna_annotation; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_qpcr_mirna_annotation (
    id_ref character varying(100),
    probe_id character varying(100),
    mirna_symbol character varying(100),
    mirna_id character varying(100),
    probeset_id int NOT NULL,
    organism character varying(100),
    gpl_id character varying(50)
);

--
-- Name: de_qpcr_mirna_annotation_pk; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_qpcr_mirna_annotation
    ADD CONSTRAINT de_qpcr_mirna_annotation_pk PRIMARY KEY (probeset_id);

