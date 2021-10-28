--
-- Name: temp_path_to_num; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE temp_path_to_num (
    c_fullname character varying(700),
    path_num int
);

--
-- Name: tempPath2NumPk; Type: CONSTRAINT; Schema: tm_cz; Owner: -
--
ALTER TABLE ONLY temp_path_to_num
    ADD CONSTRAINT tempPath2NumPk PRIMARY KEY (c_fullname);


