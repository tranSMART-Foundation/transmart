--
-- Name: qt_sq_qri_qriid; Type: SEQUENCE; Schema: i2b2demodata; Owner: -
--
CREATE SEQUENCE qt_sq_qri_qriid
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: qt_query_result_instance; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE qt_query_result_instance (
    result_instance_id serial NOT NULL,
    query_instance_id int,
    result_type_id int NOT NULL,
    set_size int,
    start_date timestamp NOT NULL,
    end_date timestamp,
    delete_flag character varying(3),
    status_type_id int NOT NULL,
    message text,
    description character varying(200),
    real_set_size int,
    obfusc_method character varying(500)
);

--
-- Name: qt_query_result_instance_pk; Type: CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY qt_query_result_instance
    ADD CONSTRAINT qt_query_result_instance_pk PRIMARY KEY (result_instance_id);

--
-- Name: qt_fk_qri_rid; Type: FK CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY qt_query_result_instance
    ADD CONSTRAINT qt_fk_qri_rid FOREIGN KEY (query_instance_id) REFERENCES qt_query_instance(query_instance_id);

--
-- Name: qt_fk_qri_rtid; Type: FK CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY qt_query_result_instance
    ADD CONSTRAINT qt_fk_qri_rtid FOREIGN KEY (result_type_id) REFERENCES qt_query_result_type(result_type_id);

--
-- Name: qt_fk_qri_stid; Type: FK CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY qt_query_result_instance
    ADD CONSTRAINT qt_fk_qri_stid FOREIGN KEY (status_type_id) REFERENCES qt_query_status_type(status_type_id);

