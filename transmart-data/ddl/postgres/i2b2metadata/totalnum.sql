--
-- Name: totalnum; Type: TABLE; Schema: i2b2metadata; Owner: -
--
CREATE TABLE totalnum (
    c_fullname character varying(850),
    agg_date timestamp,
    agg_count int,
    typeflag_cd character varying(3)
);

--
-- Name: totalnum_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX totalnum_idx ON totalnum USING btree (c_fullname, agg_date, typeflag_cd);

