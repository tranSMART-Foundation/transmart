--
-- Name: search_secure_object; Type: TABLE; Schema: searchapp; Owner: -
--
CREATE TABLE search_secure_object (
    search_secure_object_id int NOT NULL,
    bio_data_id int,
    display_name character varying(100),
    data_type character varying(200),
    bio_data_unique_id character varying(200)
);

--
-- Name: search_sec_obj_pk; Type: CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY search_secure_object
    ADD CONSTRAINT search_sec_obj_pk PRIMARY KEY (search_secure_object_id);

--
-- Name: tf_trg_search_sec_obj_id(); Type: FUNCTION; Schema: searchapp; Owner: -
--
CREATE FUNCTION tf_trg_search_sec_obj_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.search_secure_object_id is null then
        select nextval('searchapp.seq_search_data_id') into new.search_secure_object_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_search_sec_obj_id; Type: TRIGGER; Schema: searchapp; Owner: -
--
CREATE TRIGGER trg_search_sec_obj_id BEFORE INSERT ON search_secure_object FOR EACH ROW EXECUTE PROCEDURE tf_trg_search_sec_obj_id();

