--
-- Name: etl_settings_id_seq; Type: SEQUENCE; Schema: tm_cz; Owner: -
--
CREATE SEQUENCE etl_settings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: etl_settings; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE etl_settings (
    id int NOT NULL,
    paramname character varying(20) NOT NULL,
    paramvalue character varying(255) NOT NULL
);

--
-- Name: etl_settings_pk; Type: CONSTRAINT; Schema: tm_cz; Owner: -
--
ALTER TABLE ONLY etl_settings
    ADD CONSTRAINT etl_settings_pk PRIMARY KEY (id);

--
-- Name: etl_settings_uk; Type: CONSTRAINT; Schema: tm_cz; Owner: -
--
ALTER TABLE ONLY etl_settings
    ADD CONSTRAINT etl_settings_uk UNIQUE (paramname);

--
-- Name: tf_trg_etl_settings_id(); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION tf_trg_etl_settings_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.id is null then
        select nextval('tm_cz.etl_settings_id_seq') into new.id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_etl_settings_id; Type: TRIGGER; Schema: tm_cz; Owner: -
--
CREATE TRIGGER trg_etl_settings_id BEFORE INSERT ON tm_cz.etl_settings FOR EACH ROW EXECUTE PROCEDURE tf_trg_etl_settings_id();

