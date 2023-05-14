--
-- Name: i2b2_load_path; Type: TABLE; Schema: tm_wz; Owner: -
--
CREATE TABLE i2b2_load_path (
    path character varying(700),
    record_id character(10)
);

--
-- Name: i2b2_load_path_pk; Type: CONSTRAINT; Schema: tm_wz; Owner: -
--
ALTER TABLE ONLY i2b2_load_path
    ADD CONSTRAINT i2b2_load_path_pk PRIMARY KEY (path, record_id);
