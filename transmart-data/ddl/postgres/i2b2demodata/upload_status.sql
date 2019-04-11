--
-- Name: sq_uploadstatus_uploadid; Type: SEQUENCE; Schema: i2b2demodata; Owner: -
--
CREATE SEQUENCE sq_uploadstatus_uploadid
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: upload_status; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE upload_status (
    upload_id serial NOT NULL,
    upload_label character varying(500) NOT NULL,
    user_id character varying(100) NOT NULL,
    source_cd character varying(50) NOT NULL,
    no_of_record bigint,	--bigint in i2b2
    loaded_record bigint,	--bigint in i2b2
    deleted_record bigint,	--bigint in i2b2
    load_date timestamp NOT NULL,
    end_date timestamp,
    load_status character varying(100),
    message text,
    input_file_name text,
    log_file_name text,
    transform_name character varying(500)
);

--
-- Name: pk_up_upstatus_uploadid; Type: CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY upload_status
    ADD CONSTRAINT pk_up_upstatus_uploadid PRIMARY KEY (upload_id);

