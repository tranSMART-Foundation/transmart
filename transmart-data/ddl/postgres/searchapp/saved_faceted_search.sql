--
-- Name: saved_faceted_search; Type: TABLE; Schema: searchapp; Owner: -
--
CREATE TABLE saved_faceted_search (
    saved_faceted_search_id int NOT NULL,
    user_id int NOT NULL,
    name character varying(100) NOT NULL,
    keywords character varying(4000) NOT NULL,
    create_dt timestamp DEFAULT now(),
    modified_dt timestamp,
    search_type character varying(50) DEFAULT 'FACETED_SEARCH'::character varying NOT NULL,
    analysis_ids character varying(4000)
);

--
-- Name: saved_faceted_search_pk; Type: CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY saved_faceted_search
    ADD CONSTRAINT saved_faceted_search_pk PRIMARY KEY (saved_faceted_search_id);

--
-- Name: u_saved_search__user_id_name; Type: CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY saved_faceted_search
    ADD CONSTRAINT u_saved_search__user_id_name UNIQUE (user_id, name);

--
-- Name: tf_trg_saved_faceted_search_id(); Type: FUNCTION; Schema: searchapp; Owner: -
--
CREATE FUNCTION tf_trg_saved_faceted_search_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.saved_faceted_search_id is null then
	select nextval('searchapp.seq_saved_faceted_search_id') into new.saved_faceted_search_id;
    end if;

    if new.create_dt is null then
	new.create_dt := now();
    end if;

    return new;
end;
$$;

--
-- Name: trg_saved_faceted_search_id; Type: TRIGGER; Schema: searchapp; Owner: -
--
CREATE TRIGGER trg_saved_faceted_search_id BEFORE INSERT ON saved_faceted_search FOR EACH ROW EXECUTE PROCEDURE tf_trg_saved_faceted_search_id();

--
-- Name: tf_trg_upd_saved_faceted_search(); Type: FUNCTION; Schema: searchapp; Owner: -
--
CREATE FUNCTION tf_trg_upd_saved_faceted_search() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.modified_dt is null then
	new.modified_dt := now();
    end if;

    return new;
end;
$$;

--
-- Name: trg_upd_saved_faceted_search; Type: TRIGGER; Schema: searchapp; Owner: -
--
CREATE TRIGGER trg_upd_saved_faceted_search BEFORE UPDATE ON saved_faceted_search FOR EACH ROW EXECUTE PROCEDURE tf_trg_upd_saved_faceted_search();

--
-- Name: saved_faceted_search_user_id; Type: FK CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY saved_faceted_search
    ADD CONSTRAINT saved_faceted_search_user_id FOREIGN KEY (user_id) REFERENCES search_auth_user(id);

--
-- Name: seq_saved_faceted_search_id; Type: SEQUENCE; Schema: searchapp; Owner: -
--
CREATE SEQUENCE seq_saved_faceted_search_id
    START WITH 278
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 20;

