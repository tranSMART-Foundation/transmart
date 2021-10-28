--
-- Name: temp_final_counts_by_concept; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE temp_final_counts_by_concept (
    c_fullname character varying(700),
    num_patients int
);

--
-- Name: tempFinalCountsByConceptName; Type: CONSTRAINT; Schema: tm_cz; Owner: -
--
CREATE INDEX tempFinalCountsByConceptName ON temp_final_counts_by_concept USING BTREE (c_fullname);


