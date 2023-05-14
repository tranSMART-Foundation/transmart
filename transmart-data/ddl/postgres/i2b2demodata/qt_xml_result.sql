--
-- Name: qt_sq_qxr_xrid; Type: SEQUENCE; Schema: i2b2demodata; Owner: -
--
CREATE SEQUENCE qt_sq_qxr_xrid
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: qt_xml_result; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE qt_xml_result (
    xml_result_id serial NOT NULL,
    result_instance_id int,
    xml_value text
);

--
-- Name: qt_xml_result_pk; Type: CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY qt_xml_result
    ADD CONSTRAINT qt_xml_result_pk PRIMARY KEY (xml_result_id);

--
-- Name: qt_fk_xmlr_riid; Type: FK CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY qt_xml_result
    ADD CONSTRAINT qt_fk_xmlr_riid FOREIGN KEY (result_instance_id) REFERENCES qt_query_result_instance(result_instance_id);

