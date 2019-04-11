--
-- Name: crc_analysis_job; Type: TABLE; Schema: i2b2hive; Owner: -
--
CREATE TABLE crc_analysis_job (
    job_id character varying(10),
    queue_name character varying(50),
    status_type_id int,
    domain_id character varying(255),
    project_id character varying(500),
    user_id character varying(255),
    request_xml text,
    create_date timestamp,
    update_date timestamp
);
--
-- Name: analsis_job_pk; Type: CONSTRAINT; Schema: i2b2hive; Owner: -
--
ALTER TABLE ONLY crc_analysis_job
    ADD CONSTRAINT analsis_job_pk PRIMARY KEY (job_id);
--
-- Name: crc_idx_aj_qnstid; Type: INDEX; Schema: i2b2hive; Owner: -
--
CREATE INDEX crc_idx_aj_qnstid ON crc_analysis_job USING btree (queue_name, status_type_id);
