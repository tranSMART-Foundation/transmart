--
-- Name: temp_path_counts; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE temp_path_counts (
    path_num int,
    num_patients int
);

--
-- Name: tempPathCountsPk; Type: CONSTRAINT; Schema: tm_cz; Owner: -
--
ALTER TABLE ONLY temp_path_counts
    ADD CONSTRAINT tempPathCountsPk PRIMARY KEY (path_num);


