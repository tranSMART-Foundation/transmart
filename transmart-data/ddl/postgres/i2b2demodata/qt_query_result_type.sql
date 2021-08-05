--
-- Name: qt_sq_qr_qrid; Type: SEQUENCE; Schema: i2b2demodata; Owner: -
--
CREATE SEQUENCE qt_sq_qr_qrid
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: qt_query_result_type; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE qt_query_result_type (
    result_type_id int NOT NULL,
    name character varying(100),
    description character varying(200),
    display_type_id character varying(500),
    visual_attribute_type_id character varying(3),
    user_role_cd character varying(255),
    classname character varying(200)
);

--
-- Name: qt_query_result_type_pk; Type: CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY qt_query_result_type
    ADD CONSTRAINT qt_query_result_type_pk PRIMARY KEY (result_type_id);

