--
-- Name: temp_dim_count_ont; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE temp_dim_count_ont (
    c_fullname character varying(700),
    c_basecode character varying(50),
    c_hlevel int
);

--
-- Name: dimCountOntA; Type: INDEX; Schema: tm_cz; Owner: -
--
CREATE INDEX dimCountOntA ON temp_dim_count_ont(c_fullname);

--
-- Name: dimCountOntA; Type: INDEX; Schema: tm_cz; Owner: -
--
CREATE INDEX dimCountOntB ON temp_dim_count_ont(c_fullname text_pattern_ops);


