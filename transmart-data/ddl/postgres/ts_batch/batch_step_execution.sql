--
-- Name: batch_step_execution; Type: TABLE; Schema: ts_batch; Owner: -
--
CREATE TABLE batch_step_execution (
    step_execution_id int NOT NULL,
    version int NOT NULL,
    step_name character varying(100) NOT NULL,
    job_execution_id int NOT NULL,
    start_time timestamp NOT NULL,
    end_time timestamp,
    status character varying(10),
    commit_count int,
    read_count int,
    filter_count int,
    write_count int,
    read_skip_count int,
    write_skip_count int,
    process_skip_count int,
    rollback_count int,
    exit_code character varying(2500),
    exit_message character varying(2500),
    last_updated timestamp
);

--
-- Name: batch_step_execution_pk; Type: CONSTRAINT; Schema: ts_batch; Owner: -
--
ALTER TABLE ONLY batch_step_execution
    ADD CONSTRAINT batch_step_execution_pk PRIMARY KEY (step_execution_id);

--
-- Name: job_exec_step_fk; Type: FK CONSTRAINT; Schema: ts_batch; Owner: -
--
ALTER TABLE ONLY batch_step_execution
    ADD CONSTRAINT job_exec_step_fk FOREIGN KEY (job_execution_id) REFERENCES batch_job_execution(job_execution_id);

