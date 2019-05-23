--
-- Name: cz_job_message; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE cz_job_message (
    job_id int NOT NULL,
    message_id int,
    message_line int,
    message_procedure character varying(100),
    info_message character varying(2000),
    seq_id int NOT NULL
);

--
-- Name: tf_trg_cz_message_seq_id(); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION tf_trg_cz_message_seq_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.seq_id is null then
	select nextval('tm_cz.seq_cz_job_message') into new.seq_id ;
    end if;

    return new;
end;
$$;

--
-- Name: trg_cz_message_seq_id; Type: TRIGGER; Schema: tm_cz; Owner: -
--
CREATE TRIGGER trg_cz_message_seq_id BEFORE INSERT ON cz_job_message FOR EACH ROW EXECUTE PROCEDURE tf_trg_cz_message_seq_id();

--
-- Name: seq_cz_job_message; Type: SEQUENCE; Schema: tm_cz; Owner: -
--
CREATE SEQUENCE seq_cz_job_message
    START WITH 988
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 2;

