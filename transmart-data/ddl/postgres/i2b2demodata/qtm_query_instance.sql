--
-- Name: qtm_sq_qi_qiid; Type: SEQUENCE; Schema: i2b2demodata; Owner: -
--
CREATE SEQUENCE qtm_sq_qi_qiid
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: qtm_query_instance; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE qtm_query_instance (
    query_instance_id serial NOT NULL,
    query_master_id int,
    user_id character varying(50) NOT NULL,
    group_id character varying(50) NOT NULL,
    batch_mode character varying(50),
    start_date timestamp NOT NULL,
    end_date timestamp,
    delete_flag character varying(3),
    status_type_id int,
    message text
);

--
-- Name: qtm_query_instance_pk; Type: CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY qtm_query_instance
    ADD CONSTRAINT qtm_query_instance_pk PRIMARY KEY (query_instance_id);

--
-- Name: qtm_fk_qi_mid; Type: FK CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY qtm_query_instance
    ADD CONSTRAINT qtm_fk_qi_mid FOREIGN KEY (query_master_id) REFERENCES qtm_query_master(query_master_id);

--
-- Name: qtm_fk_qi_stid; Type: FK CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY qtm_query_instance
    ADD CONSTRAINT qtm_fk_qi_stid FOREIGN KEY (status_type_id) REFERENCES qt_query_status_type(status_type_id);

--
-- Name: qtm_idx_qi_ugid; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX qtm_idx_qi_ugid ON qtm_query_instance USING btree (user_id, group_id);

--
-- Name: qtm_idx_qi_mstartid; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX qtm_idx_qi_mstartid ON qtm_query_instance USING btree (query_master_id, start_date);
