--
-- Name: workplace; Type: TABLE; Schema: i2b2workdata; Owner: -
--
CREATE TABLE workplace (
    c_name character varying(255) NOT NULL,
    c_user_id character varying(255) NOT NULL,
    c_group_id character varying(255) NOT NULL,
    c_share_id character varying(255),
    c_index character varying(255) NOT NULL,
    c_parent_index character varying(255),
    c_visualattributes character(3) NOT NULL,
    c_protected_access character(1),
    c_tooltip character varying(255),
    c_work_xml text,
    c_work_xml_schema text,
    c_work_xml_i2b2_type character varying(255),
    c_entry_date timestamp,
    c_change_date timestamp,
    c_status_cd character(1)
);
--
-- Name: workplace_pk; Type: CONSTRAINT; Schema: i2b2workdata; Owner: -
--
ALTER TABLE ONLY workplace
    ADD CONSTRAINT workplace_pk PRIMARY KEY (c_index);
