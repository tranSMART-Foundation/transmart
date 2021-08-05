--
-- Name: totalnum_report; Type: TABLE; Schema: i2b2metadata; Owner: -
--
CREATE TABLE totalnum_report (
    c_fullname character varying(850),
    agg_date character varying(50),
    agg_count int
);

--
-- Name: totalnum_report_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX totalnum_report_idx ON totalnum_report USING btree (c_fullname);

