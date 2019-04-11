--
-- Name: qt_sq_qper_pecid; Type: SEQUENCE; Schema: i2b2demodata; Owner: -
--
CREATE SEQUENCE qt_sq_qper_pecid
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: qt_patient_enc_collection; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE qt_patient_enc_collection (
    patient_enc_coll_id serial NOT NULL,
    result_instance_id int,
    set_index int,
    patient_num int,
    encounter_num int
);

--
-- Name: qt_patient_enc_coll_pk; Type: CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY qt_patient_enc_collection
    ADD CONSTRAINT qt_patient_enc_coll_pk PRIMARY KEY (patient_enc_coll_id);

--
-- Name: qt_fk_pesc_ri; Type: FK CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY qt_patient_enc_collection
    ADD CONSTRAINT qt_fk_pesc_ri FOREIGN KEY (result_instance_id) REFERENCES qt_query_result_instance(result_instance_id);

