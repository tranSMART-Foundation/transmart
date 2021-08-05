--
-- Name: concept_counts; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE concept_counts (
    concept_path character varying(500),
    parent_concept_path character varying(500),
    patient_count int
);

--
-- Name: concept_counts_idx; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX concept_counts_IDX ON concept_counts USING btree (concept_path);


