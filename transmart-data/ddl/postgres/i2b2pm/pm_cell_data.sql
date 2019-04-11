--
-- Name: pm_cell_data; Type: TABLE; Schema: i2b2pm; Owner: -
--
CREATE TABLE pm_cell_data (
    cell_id character varying(50) NOT NULL,
    project_path character varying(255) NOT NULL,
    name character varying(255),
    method_cd character varying(255),
    url character varying(255),
    can_override int,
    change_date timestamp,
    entry_date timestamp,
    changeby_char character varying(50),
    status_cd character varying(50)
);
--
-- Name: pm_cell_data_pk; Type: CONSTRAINT; Schema: i2b2pm; Owner: -
--
ALTER TABLE ONLY pm_cell_data
    ADD CONSTRAINT pm_cell_data_pk PRIMARY KEY (cell_id,project_path);
