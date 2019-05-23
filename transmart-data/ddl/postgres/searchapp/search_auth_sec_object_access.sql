--
-- Name: search_auth_sec_object_access; Type: TABLE; Schema: searchapp; Owner: -
--
CREATE TABLE search_auth_sec_object_access (
    auth_sec_obj_access_id int NOT NULL,
    auth_principal_id int,
    secure_object_id int,
    secure_access_level_id int
);

--
-- Name: sch_sec_a_a_s_a_pk; Type: CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY search_auth_sec_object_access
    ADD CONSTRAINT sch_sec_a_a_s_a_pk PRIMARY KEY (auth_sec_obj_access_id);

--
-- Name: tf_trg_search_au_obj_access_id(); Type: FUNCTION; Schema: searchapp; Owner: -
--
CREATE FUNCTION tf_trg_search_au_obj_access_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.auth_sec_obj_access_id is null then
        select nextval('searchapp.seq_search_data_id') into new.auth_sec_obj_access_id ;
    end if;

    return new;
end;
$$;

--
-- Name: trg_search_au_obj_access_id; Type: TRIGGER; Schema: searchapp; Owner: -
--
CREATE TRIGGER trg_search_au_obj_access_id BEFORE INSERT ON search_auth_sec_object_access FOR EACH ROW EXECUTE PROCEDURE tf_trg_search_au_obj_access_id();

--
-- Name: sch_sec_a_u_fk; Type: FK CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY search_auth_sec_object_access
    ADD CONSTRAINT sch_sec_a_u_fk FOREIGN KEY (auth_principal_id) REFERENCES search_auth_principal(id);

--
-- Name: sch_sec_s_a_l_fk; Type: FK CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY search_auth_sec_object_access
    ADD CONSTRAINT sch_sec_s_a_l_fk FOREIGN KEY (secure_access_level_id) REFERENCES search_sec_access_level(search_sec_access_level_id);

--
-- Name: sch_sec_s_o_fk; Type: FK CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY search_auth_sec_object_access
    ADD CONSTRAINT sch_sec_s_o_fk FOREIGN KEY (secure_object_id) REFERENCES search_secure_object(search_secure_object_id);

