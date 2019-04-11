--
-- Name: bio_analysis_cohort_xref; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_analysis_cohort_xref (
    study_id character varying(255),
    cohort_id character varying(255),
    analysis_cd character varying(255),
    bio_assay_analysis_id int NOT NULL
);

--
-- Name: bio_analysis_cohort_xref_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_analysis_cohort_xref
    ADD CONSTRAINT bio_analysis_cohort_xref_pk PRIMARY KEY (bio_assay_analysis_id);

