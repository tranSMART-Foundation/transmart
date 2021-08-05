--
-- Name: hive_cell_params; Type: TABLE; Schema: i2b2hive; Owner: -
--
CREATE TABLE hive_cell_params (
    id int NOT NULL,
    datatype_cd character varying(50),
    cell_id character varying(50) NOT NULL,
    param_name_cd character varying(200) NOT NULL,
    value text,
    change_date timestamp,
    entry_date timestamp,
    changeby_char character varying(50),
    status_cd character varying(50)
);
--
-- Name: hive_ce__pk; Type: CONSTRAINT; Schema: i2b2hive; Owner: -
--
ALTER TABLE ONLY hive_cell_params
    ADD CONSTRAINT hive_ce__pk PRIMARY KEY (id);
