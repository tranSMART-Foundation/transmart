--
-- Name: cz_data_file; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE cz_data_file (
    file_name character varying(200),
    provider character varying(200),
    extraction_date timestamp,
    location character varying(500),
    data_id int NOT NULL,
    contact_id int,
    exp_record_cnt int,
    act_record_cnt int,
    url character varying(500),
    description character varying(2000),
    data_file_id int NOT NULL
);

--
-- Name: cz_data_file_pk; Type: CONSTRAINT; Schema: tm_cz; Owner: -
--
ALTER TABLE ONLY cz_data_file
    ADD CONSTRAINT cz_data_file_pk PRIMARY KEY (data_file_id);

--
-- Name: tf_trg_cz_data_file_id(); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION tf_trg_cz_data_file_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.data_file_id is null then
	select nextval('tm_cz.seq_cz_data_file') into new.data_file_id ;
    end if;

    return new;
end;
$$;

--
-- Name: trg_cz_data_file_id; Type: TRIGGER; Schema: tm_cz; Owner: -
--
CREATE TRIGGER trg_cz_data_file_id BEFORE INSERT ON cz_data_file FOR EACH ROW EXECUTE PROCEDURE tf_trg_cz_data_file_id();

--
-- Name: cz_data_file_cz_data_fk1; Type: FK CONSTRAINT; Schema: tm_cz; Owner: -
--
ALTER TABLE ONLY cz_data_file
    ADD CONSTRAINT cz_data_file_cz_data_fk1 FOREIGN KEY (data_id) REFERENCES cz_data(data_id);

--
-- Name: seq_cz_data_file; Type: SEQUENCE; Schema: tm_cz; Owner: -
--
CREATE SEQUENCE seq_cz_data_file
    START WITH 6
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 2;

