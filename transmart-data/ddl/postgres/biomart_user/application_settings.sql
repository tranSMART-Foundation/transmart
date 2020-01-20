--
-- Name: application_settings; Type: TABLE; Schema: biomart_user; Owner: -
--
CREATE TABLE application_settings (
    id int NOT NULL,
    version int NOT NULL,
    fieldname character varying(255) NOT NULL,
    fieldvalue character varying(2000) NOT NULL,
    last_updated timestamp NOT NULL,
    userid int NOT NULL
);

--
-- Name: application_settings_pk; Type: CONSTRAINT; Schema: biomart_user; Owner: -
--
ALTER TABLE ONLY application_settings
    ADD CONSTRAINT application_settings_pk PRIMARY KEY (id);

--
-- Name: tf_trg_application_settings_id(); Type: FUNCTION; Schema: biomart_user; Owner: -
--
CREATE FUNCTION tf_trg_application_settings_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.id is null then
        select nextval('biomart_user.application_settings_id_seq') into new.id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_application_settings_id; Type: TRIGGER; Schema: biomart_user; Owner: -
--
CREATE TRIGGER trg_application_settings_id BEFORE INSERT ON biomart_user.application_settings FOR EACH ROW EXECUTE PROCEDURE tf_trg_application_settings_id();
