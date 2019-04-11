--
-- Name: pm_role_requirement; Type: TABLE; Schema: i2b2pm; Owner: -
--
CREATE TABLE pm_role_requirement (
    table_cd character varying(50) NOT NULL,
    column_cd character varying(50) NOT NULL,
    read_hivemgmt_cd character varying(50) NOT NULL,
    write_hivemgmt_cd character varying(50) NOT NULL,
    name_char character varying(2000),
    change_date timestamp,
    entry_date timestamp,
    changeby_char character varying(50),
    status_cd character varying(50)
);
--
-- Name: pm_role_requirement_pk; Type: CONSTRAINT; Schema: i2b2pm; Owner: -
--
ALTER TABLE ONLY pm_role_requirement
    ADD CONSTRAINT pm_role_requirement_pk PRIMARY KEY (table_cd,column_cd,read_hivemgmt_cd,write_hivemgmt_cd);
