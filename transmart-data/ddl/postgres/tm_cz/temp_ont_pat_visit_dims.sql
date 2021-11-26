--
-- Name: temp_ont_pat_visit_dims; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE temp_ont_pat_visit_dims (
    c_fullname character varying(700),
    c_basecode character varying(50),
    c_facttablecolumn character varying(50),
    c_tablename character varying(50),
    c_columnname character varying(50),
    c_operator character varying(10),
    c_dimcode character varying(700),
    numpats int
);

--
-- Name: ontPatVisitDimsfname; Type: INDEX; Schema: tm_cz; Owner: -
--
CREATE INDEX ontPatVisitDimsfname ON temp_ont_pat_visit_dims(c_fullname);

