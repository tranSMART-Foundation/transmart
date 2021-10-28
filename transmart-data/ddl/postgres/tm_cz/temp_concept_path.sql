--
-- Name: temp_concept_path; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE temp_concept_path (
    path_num int,
    c_basecode character varying(50)
);

--
-- Name: tempConceptPathPk; Type: CONSTRAINT; Schema: tm_cz; Owner: -
--
ALTER TABLE ONLY temp_concept_path
    ADD CONSTRAINT tempConceptPathPk PRIMARY KEY (c_basecode, path_num);


