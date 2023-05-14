--
-- Name: i2b2_load_path_with_count; Type: TABLE; Schema: tm_wz; Owner: -
--
CREATE TABLE i2b2_load_path_with_count (
    c_fullname character varying(700),
    nbr_children int
);

--
-- Name: i2b2_load_path_with_count_pk; Type: CONSTRAINT; Schema: tm_wz; Owner: -
--
ALTER TABLE ONLY i2b2_load_path_with_count
    ADD CONSTRAINT i2b2_load_path_with_count_pk PRIMARY KEY (c_fullname);
