--
-- Name: search_keyword_term; Type: TABLE; Schema: searchapp; Owner: -
--
CREATE TABLE search_keyword_term (
    keyword_term character varying(200),
    search_keyword_id int,
    rank int,
    search_keyword_term_id int NOT NULL,
    term_length int,
    owner_auth_user_id int
);

--
-- Name: search_kw_term_pk; Type: CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY search_keyword_term
    ADD CONSTRAINT search_kw_term_pk PRIMARY KEY (search_keyword_term_id);

--
-- Name: search_kw_term_skid_idx; Type: INDEX; Schema: searchapp; Owner: -
--
CREATE INDEX search_kw_term_skid_idx ON search_keyword_term USING btree (search_keyword_id);

--
-- Name: tf_trg_search_keyword_term_id(); Type: FUNCTION; Schema: searchapp; Owner: -
--
CREATE FUNCTION tf_trg_search_keyword_term_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.search_keyword_term_id is null then
        select nextval('searchapp.seq_search_data_id') into new.search_keyword_term_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_search_keyword_term_id; Type: TRIGGER; Schema: searchapp; Owner: -
--
CREATE TRIGGER trg_search_keyword_term_id BEFORE INSERT ON search_keyword_term FOR EACH ROW EXECUTE PROCEDURE tf_trg_search_keyword_term_id();

--
-- Name: search_kw_fk; Type: FK CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY search_keyword_term
    ADD CONSTRAINT search_kw_fk FOREIGN KEY (search_keyword_id) REFERENCES search_keyword(search_keyword_id);

