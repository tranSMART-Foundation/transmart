--
-- Name: search_keyword; Type: TABLE; Schema: searchapp; Owner: -
--
CREATE TABLE search_keyword (
    keyword character varying(400),
    bio_data_id int,
    unique_id character varying(500) NOT NULL,
    search_keyword_id int NOT NULL,
    data_category character varying(200) NOT NULL,
    source_code character varying(100),
    display_data_category character varying(200),
    owner_auth_user_id int
);

--
-- Name: search_keyword_uk; Type: CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY search_keyword
    ADD CONSTRAINT search_keyword_uk UNIQUE (unique_id, data_category);

--
-- Name: search_kw_pk; Type: CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY search_keyword
    ADD CONSTRAINT search_kw_pk PRIMARY KEY (search_keyword_id);

--
-- Name: sk_data_cat_idx; Type: INDEX; Schema: searchapp; Owner: -
--
CREATE INDEX sk_data_cat_idx ON search_keyword USING btree (data_category);

--
-- Name: sk_data_display_cat_idx; Type: INDEX; Schema: searchapp; Owner: -
--
CREATE INDEX sk_data_display_cat_idx ON search_keyword USING btree (data_category, display_data_category);

--
-- Name: search_keyword_idx1; Type: INDEX; Schema: searchapp; Owner: -
--
CREATE INDEX search_keyword_idx1 ON search_keyword USING btree (keyword);

--
-- Name: search_keyword_idx2; Type: INDEX; Schema: searchapp; Owner: -
--
CREATE INDEX search_keyword_idx2 ON search_keyword USING btree (bio_data_id);

--
-- Name: search_keyword_idx3; Type: INDEX; Schema: searchapp; Owner: -
--
CREATE INDEX search_keyword_idx3 ON search_keyword USING btree (owner_auth_user_id);

--
-- Name: tf_trg_search_keyword_id(); Type: FUNCTION; Schema: searchapp; Owner: -
--
CREATE FUNCTION tf_trg_search_keyword_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.search_keyword_id is null then
        select nextval('searchapp.seq_search_data_id') into new.search_keyword_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_search_keyword_id; Type: TRIGGER; Schema: searchapp; Owner: -
--
CREATE TRIGGER trg_search_keyword_id BEFORE INSERT ON search_keyword FOR EACH ROW EXECUTE PROCEDURE tf_trg_search_keyword_id();

