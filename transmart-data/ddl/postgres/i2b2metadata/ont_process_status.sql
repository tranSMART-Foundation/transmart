--
-- Name: ont_sq_ps_prid; Type: SEQUENCE; Schema: i2b2metadata; Owner: -
--
CREATE SEQUENCE ont_sq_ps_prid
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: ont_process_status; Type: TABLE; Schema: i2b2metadata; Owner: -
--
CREATE TABLE ont_process_status (
    process_id serial NOT NULL,
    process_type_cd character varying(50),
    start_date timestamp,
    end_date timestamp,
    process_step_cd character varying(50),
    process_status_cd character varying(50),
    crc_upload_id int,
    status_cd character varying(50),
    message character varying(2000),
    entry_date timestamp,
    change_date timestamp,
    changedby_char character(50)
);

--
-- Name: ont_process_status_pk; Type: CONSTRAINT; Schema: i2b2metadata; Owner: -
--
ALTER TABLE ONLY ont_process_status
    ADD CONSTRAINT ont_process_status_pk PRIMARY KEY (process_id);

