--
-- Name: wrk_clinical_data; Type: TABLE; Schema: tm_wz; Owner: -
--
CREATE TABLE wrk_clinical_data (
    study_id character varying(25),
    site_id character varying(50),
    subject_id character varying(100),
    visit_name character varying(100),
    data_label character varying(500),
    data_value character varying(500),
    dval_char character varying(500),
    dval_num decimal(18,5),
    category_cd character varying(250),
    etl_job_id int,
    etl_date timestamp,
    sourcesystem_cd character varying(50),
    usubjid character varying(200),
    uencid character varying(200),
    category_path character varying(1000),
    data_type character varying(10),
    data_label_ctrl_vocab_code character varying(200),
    data_value_ctrl_vocab_code character varying(500),
    data_label_components character varying(1000),
    units_cd character varying(50),
    visit_date timestamp,
    node_name character varying(1000),
    link_type character varying(20),
    link_value character varying(200),
    end_date timestamp,
    visit_reference character varying(100),
    date_ind character(1),
    obs_string character varying(100),
    rec_num int,
    valuetype_cd character varying(50),
    leaf_node character varying(2000),
    modifier_cd character varying(100),
    ctrl_vocab_code character varying(200),
    date_timestamp timestamp,
    sample_cd character varying(200)
);

--
-- Name: wrk_cd_idx; Type: INDEX; Schema: tm_wz; Owner: -
--
-- CREATE INDEX wrk_cd_idx ON wrk_clinical_data USING btree (data_type, data_value, visit_name, data_label, category_cd, usubjid);
