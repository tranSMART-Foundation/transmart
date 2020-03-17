--
-- Name: de_subject_protein_data; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_subject_protein_data (
    trial_name character varying(100),
    protein_annotation_id int,
    component character varying(100),
    patient_id int,
    gene_symbol character varying(100),
    gene_id character varying(100), --defined as NUMBER(10,0) in oracle
    assay_id int,
    subject_id character varying(100),
    intensity double precision,
    zscore double precision,
    log_intensity double precision,
    timepoint character varying(250),
    partition_id int
);

--
-- Name: fk_protein_annotation_id; Type: FK CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_subject_protein_data
    ADD CONSTRAINT fk_protein_annotation_id FOREIGN KEY (protein_annotation_id) REFERENCES de_protein_annotation(id);

