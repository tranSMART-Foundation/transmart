--
-- Name: qt_sq_qpr_pcid; Type: SEQUENCE; Schema: i2b2demodata; Owner: -
--
CREATE SEQUENCE qt_sq_qpr_pcid
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: qt_patient_set_collection; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE qt_patient_set_collection (
    patient_set_coll_id bigserial NOT NULL,
    result_instance_id int,
    set_index int,
    patient_num int
);

--
-- Name: qt_patient_set_coll_pk; Type: CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY qt_patient_set_collection
    ADD CONSTRAINT qt_patient_set_coll_pk PRIMARY KEY (patient_set_coll_id);

--
-- Name: qt_fk_psc_ri; Type: FK CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY qt_patient_set_collection
    ADD CONSTRAINT qt_fk_psc_ri FOREIGN KEY (result_instance_id) REFERENCES qt_query_result_instance(result_instance_id);

--
-- Name: qt_idx_qpsc_riid; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX qt_idx_qpsc_riid ON qt_patient_set_collection USING btree (result_instance_id);

