--
-- Name: import_xnat_configuration; Type: TABLE; Schema: searchapp; Owner: -
--
CREATE TABLE import_xnat_configuration
(
  id int NOT NULL,
  version int NOT NULL,
  name character varying(255) NOT NULL,
  description text,
  url character varying(255) NOT NULL,
  username character varying(255) NOT NULL,
  project character varying(255) NOT NULL,
  node character varying(255) NOT NULL
);

--
-- Name: import_xnat_config_pk; Type: CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY import_xnat_configuration
    ADD CONSTRAINT import_xnat_config_pk PRIMARY KEY (id);

--
-- Name: tf_trg_import_xnat_config_id(); Type: FUNCTION; Schema: searchapp; Owner: -
--
CREATE FUNCTION tf_trg_import_xnat_config_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.id is null then
        select nextval('searchapp.seq_search_data_id') into new.id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_import_xnat_config_id; Type: TRIGGER; Schema: searchapp; Owner: -
--
CREATE TRIGGER trg_import_xnat_config_id BEFORE INSERT ON import_xnat_configuration FOR EACH ROW EXECUTE PROCEDURE tf_trg_import_xnat_config_id();

