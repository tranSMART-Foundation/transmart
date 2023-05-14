--
-- Name: de_xtrial_child_map; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_xtrial_child_map (
    concept_cd character varying(50) NOT NULL,
    parent_cd int NOT NULL,
    manually_mapped int,
    study_id character varying(50)
);

--
-- Name: de_xtrial_child_map_pk; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_xtrial_child_map
    ADD CONSTRAINT de_xtrial_child_map_pk PRIMARY KEY (concept_cd);

--
-- Name: dexcm_parent_cd_fk; Type: FK CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_xtrial_child_map
    ADD CONSTRAINT dexcm_parent_cd_fk FOREIGN KEY (parent_cd) REFERENCES de_xtrial_parent_names(parent_cd);

