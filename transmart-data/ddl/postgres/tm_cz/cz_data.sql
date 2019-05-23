--
-- Name: cz_data; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE cz_data (
    data_id int NOT NULL,
    data_name character varying(200),
    technical_desc character varying(1000),
    business_desc character varying(1000),
    create_date timestamp,
    custodian_id int,
    owner_id int,
    load_freq character varying(20)
);

--
-- Name: cz_data_pk; Type: CONSTRAINT; Schema: tm_cz; Owner: -
--
ALTER TABLE ONLY cz_data
    ADD CONSTRAINT cz_data_pk PRIMARY KEY (data_id);

--
-- Name: tf_trg_cz_data_id(); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION tf_trg_cz_data_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.data_id is null then
	select nextval('tm_cz.seq_cz_data') into new.data_id ;
    end if;

    return new;
end;
$$;

--
-- Name: trg_cz_data_id; Type: TRIGGER; Schema: tm_cz; Owner: -
--
CREATE TRIGGER trg_cz_data_id BEFORE INSERT ON cz_data FOR EACH ROW EXECUTE PROCEDURE tf_trg_cz_data_id();

--
-- Name: seq_cz_data; Type: SEQUENCE; Schema: tm_cz; Owner: -
--
CREATE SEQUENCE seq_cz_data
    START WITH 5
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 2;

