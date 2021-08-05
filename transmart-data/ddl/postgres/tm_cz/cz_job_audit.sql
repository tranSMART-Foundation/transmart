--
-- Name: cz_job_audit; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE cz_job_audit (
    seq_id int NOT NULL,
    job_id int NOT NULL,
    database_name character varying(50),
    procedure_name character varying(100),
    step_desc character varying(1000),
    step_status character varying(50),
    records_manipulated int,
    step_number int,
    job_date timestamp,
    time_elapsed_secs double precision DEFAULT 0
);

--
-- Name: cz_job_audit_pk; Type: CONSTRAINT; Schema: tm_cz; Owner: -
--
ALTER TABLE ONLY cz_job_audit
    ADD CONSTRAINT cz_job_audit_pk PRIMARY KEY (seq_id);

--
-- Name: cz_job_audit_jobid_date; Type: INDEX; Schema: tm_cz; Owner: -
--
CREATE INDEX cz_job_audit_jobid_date ON cz_job_audit USING btree (job_id, job_date);

--
-- Name: tf_trg_cz_seq_id(); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION tf_trg_cz_seq_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin     
    if new.seq_id is null then
        select nextval('tm_cz.seq_cz_job_audit') into new.seq_id ;       
    end if;       

    return new;
end;
$$;

--
-- Name: trg_cz_seq_id; Type: TRIGGER; Schema: tm_cz; Owner: -
--
CREATE TRIGGER trg_cz_seq_id BEFORE INSERT ON cz_job_audit FOR EACH ROW EXECUTE PROCEDURE tf_trg_cz_seq_id();

--
-- Name: seq_cz_job_audit; Type: SEQUENCE; Schema: tm_cz; Owner: -
--
CREATE SEQUENCE seq_cz_job_audit
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 2;

