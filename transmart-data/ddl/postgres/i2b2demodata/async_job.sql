--
-- Name: async_job; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE async_job (
    id int NOT NULL,
    job_name character varying(200),
    job_status character varying(200),
    run_time character varying(200),
    job_status_time timestamp,
    last_run_on timestamp,
    viewer_url character varying(4000),
    alt_viewer_url character varying(4000),
    job_results text,
    job_inputs_json text,
    job_type character varying(20)
);

--
-- Name: async_job_pk; Type: CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY async_job
    ADD CONSTRAINT async_job_pk PRIMARY KEY (id);

