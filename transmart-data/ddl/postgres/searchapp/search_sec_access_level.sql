--
-- Name: search_sec_access_level; Type: TABLE; Schema: searchapp; Owner: -
--
CREATE TABLE search_sec_access_level (
    search_sec_access_level_id int NOT NULL,
    access_level_name character varying(200),
    access_level_value int
);

--
-- Name: search_sec_ac_level_pk; Type: CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY search_sec_access_level
    ADD CONSTRAINT search_sec_ac_level_pk PRIMARY KEY (search_sec_access_level_id);

--
-- Name: tf_trg_search_sec_acc_level_id(); Type: FUNCTION; Schema: searchapp; Owner: -
--
CREATE FUNCTION tf_trg_search_sec_acc_level_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.search_sec_access_level_id is null then
        select nextval('searchapp.seq_search_data_id') into new.search_sec_access_level_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_search_sec_acc_level_id; Type: TRIGGER; Schema: searchapp; Owner: -
--
CREATE TRIGGER trg_search_sec_acc_level_id BEFORE INSERT ON search_sec_access_level FOR EACH ROW EXECUTE PROCEDURE tf_trg_search_sec_acc_level_id();

