--
-- Name: pm_hive_data; Type: TABLE; Schema: i2b2pm; Owner: -
--
CREATE TABLE pm_hive_data (
    domain_id character varying(50) NOT NULL,
    helpurl character varying(255),
    domain_name character varying(255),
    environment_cd character varying(255),
    active numeric(1,0),
    change_date timestamp without time zone,
    entry_date timestamp without time zone,
    changeby_char character varying(50),
    status_cd character varying(50)
);
--
-- Name: pm_hive_data_pk; Type: CONSTRAINT; Schema: i2b2pm; Owner: -
--
ALTER TABLE ONLY pm_hive_data
    ADD CONSTRAINT pm_hive_data_pk PRIMARY KEY (domain_id);
