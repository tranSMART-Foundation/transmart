--
-- Name: batch_job_instance; Type: TABLE; Schema: ts_batch; Owner: -
--
CREATE TABLE batch_job_instance (
    job_instance_id int NOT NULL,
    version int,
    job_name character varying(100) NOT NULL,
    job_key character varying(32) NOT NULL
);

--
-- Name: batch_job_instance_pk; Type: CONSTRAINT; Schema: ts_batch; Owner: -
--
ALTER TABLE ONLY batch_job_instance
    ADD CONSTRAINT batch_job_instance_pk PRIMARY KEY (job_instance_id);

--
-- Name: job_inst_un; Type: CONSTRAINT; Schema: ts_batch; Owner: -
--
ALTER TABLE ONLY batch_job_instance
    ADD CONSTRAINT job_inst_un UNIQUE (job_name, job_key);

