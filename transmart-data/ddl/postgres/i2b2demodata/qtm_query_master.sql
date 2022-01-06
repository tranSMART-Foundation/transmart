--
-- Name: qtm_sq_qm_qmid; Type: SEQUENCE; Schema: i2b2demodata; Owner: -
--
CREATE SEQUENCE qtm_sq_qm_qmid
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: qtm_query_master; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE qtm_query_master (
    query_master_id serial NOT NULL,
    name character varying(250) NOT NULL,
    user_id character varying(50) NOT NULL,
    group_id character varying(50) NOT NULL,
    master_type_cd character varying(2000),
    plugin_id int,
    create_date timestamp NOT NULL,
    delete_date timestamp,
    delete_flag character varying(3),
    generated_sql text,
    request_xml text,
    i2b2_request_xml text,
    pm_xml text
);

--
-- Name: qtm_query_master_pk; Type: CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY qtm_query_master
    ADD CONSTRAINT qtm_query_master_pk PRIMARY KEY (query_master_id);

--
-- Name: qtm_idx_qm_ugid; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX qtm_idx_qm_ugid ON qtm_query_master USING btree (user_id, group_id, master_type_cd);
