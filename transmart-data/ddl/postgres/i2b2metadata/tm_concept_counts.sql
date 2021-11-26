--
-- Name: tm_concept_counts; Type: TABLE; Schema: i2b2metadata; Owner: -
--
CREATE TABLE tm_concept_counts (
    concept_path character varying(500),
    parent_concept_path character varying(500),
    patient_count int
);

--
-- Name: tm_concept_counts_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX tm_concept_counts_IDX ON tm_concept_counts USING btree (concept_path);


