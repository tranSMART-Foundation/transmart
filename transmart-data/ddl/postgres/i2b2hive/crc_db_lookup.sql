--
-- Name: crc_db_lookup; Type: TABLE; Schema: i2b2hive; Owner: -
--
CREATE TABLE crc_db_lookup (
    c_domain_id character varying(255) NOT NULL,
    c_project_path character varying(255) NOT NULL,
    c_owner_id character varying(255) NOT NULL,
    c_db_fullschema character varying(255) NOT NULL,
    c_db_datasource character varying(255) NOT NULL,
    c_db_servertype character varying(255) NOT NULL,
    c_db_nicename character varying(255),
    c_db_tooltip character varying(255),
    c_comment text,
    c_entry_date timestamp without time zone,
    c_change_date timestamp without time zone,
    c_status_cd character(1)
);
--
-- Name: crc_db_lookup_pk; Type: CONSTRAINT; Schema: i2b2hive; Owner: -
--
ALTER TABLE ONLY crc_db_lookup
    ADD CONSTRAINT crc_db_lookup_pk PRIMARY KEY (c_domain_id,c_project_path,c_owner_id);
