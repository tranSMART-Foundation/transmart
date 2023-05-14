--
-- Name: batch_job_execution; Type: TABLE; Schema: ts_batch; Owner: -
--
CREATE TABLE batch_job_execution (
    job_execution_id int NOT NULL,
    version int,
    job_instance_id int NOT NULL,
    create_time timestamp NOT NULL,
    start_time timestamp,
    end_time timestamp,
    status character varying(10),
    exit_code character varying(2500),
    exit_message character varying(2500),
    last_updated timestamp,
    job_configuration_location character varying(2500)
);

--
-- Name: batch_job_execution_; Type: CONSTRAINT; Schema: ts_batch; Owner: -
--
ALTER TABLE ONLY batch_job_execution
    ADD CONSTRAINT batch_job_execution_pk PRIMARY KEY (job_execution_id);

--
-- Name: job_inst_exec_fk; Type: FK CONSTRAINT; Schema: ts_batch; Owner: -
--
ALTER TABLE ONLY batch_job_execution
    ADD CONSTRAINT job_inst_exec_fk FOREIGN KEY (job_instance_id) REFERENCES batch_job_instance(job_instance_id);

