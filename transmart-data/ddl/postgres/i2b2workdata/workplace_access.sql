--
-- Name: workplace_access; Type: TABLE; Schema: i2b2workdata; Owner: -
--
CREATE TABLE workplace_access (
    c_table_cd character varying(255) NOT NULL,
    c_table_name character varying(255) NOT NULL,
    c_protected_access character(1),
    c_hlevel int NOT NULL,
    c_name character varying(255) NOT NULL,
    c_user_id character varying(255) NOT NULL,
    c_group_id character varying(255) NOT NULL,
    c_share_id character varying(255),
    c_index character varying(255) NOT NULL,
    c_parent_index character varying(255),
    c_visualattributes character(3) NOT NULL,
    c_tooltip character varying(255),
    c_entry_date timestamp,
    c_change_date timestamp,
    c_status_cd character(1)
);
--
-- Name: workplace_access_pk; Type: CONSTRAINT; Schema: i2b2workdata; Owner: -
--
ALTER TABLE ONLY workplace_access
    ADD CONSTRAINT workplace_access_pk PRIMARY KEY (c_index);
