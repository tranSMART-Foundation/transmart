--
-- Name: import_xnat_variable; Type: TABLE; Schema: searchapp; Owner: -
--
CREATE TABLE import_xnat_variable
(
  id int NOT NULL,
  configuration_id int NOT NULL,
  name character varying(255) NOT NULL,
  datatype character varying(255) NOT NULL,
  url character varying(255) NOT NULL
);

--
-- Name: import_xnat_var_pk; Type: CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY import_xnat_variable
    ADD CONSTRAINT import_xnat_var_pk PRIMARY KEY (id);

--
-- Name: tf_trg_import_xnat_var_id(); Type: FUNCTION; Schema: searchapp; Owner: -
--
CREATE FUNCTION tf_trg_import_xnat_var_id() RETURNS trigger
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
-- Name: trg_import_xnat_var_id; Type: TRIGGER; Schema: searchapp; Owner: -
--
CREATE TRIGGER trg_import_xnat_var_id BEFORE INSERT ON import_xnat_variable FOR EACH ROW EXECUTE PROCEDURE tf_trg_import_xnat_var_id();

--
-- Name: import_xnat_var_fk; Type: FK CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY import_xnat_variable
    ADD CONSTRAINT import_xnat_var_fk FOREIGN KEY (configuration_id) REFERENCES import_xnat_configuration(id);

