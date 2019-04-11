--
-- Name: qt_privilege; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE qt_privilege (
    protection_label_cd character varying(1500) NOT NULL,
    dataprot_cd character varying(1000),
    hivemgmt_cd character varying(1000),
    plugin_id int
);

--
-- Name: qt_privilege_pk; Type: CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY qt_privilege
    ADD CONSTRAINT qt_privilege_pk PRIMARY KEY (protection_label_cd);

