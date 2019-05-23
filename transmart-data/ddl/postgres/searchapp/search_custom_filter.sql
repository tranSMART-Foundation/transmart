--
-- Name: search_custom_filter; Type: TABLE; Schema: searchapp; Owner: -
--
CREATE TABLE search_custom_filter (
    search_custom_filter_id int NOT NULL,
    search_user_id int NOT NULL,
    name character varying(200) NOT NULL,
    description character varying(2000),
    private character(1) DEFAULT 'N'::bpchar NOT NULL
);

--
-- Name: search_custom_filter_pk; Type: CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY search_custom_filter
    ADD CONSTRAINT search_custom_filter_pk PRIMARY KEY (search_custom_filter_id);

--
-- Name: tf_trg_search_custom_filter_id(); Type: FUNCTION; Schema: searchapp; Owner: -
--
CREATE FUNCTION tf_trg_search_custom_filter_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin 
    if new.search_custom_filter_id is null then
        select nextval('searchapp.seq_search_data_id') into new.search_custom_filter_id ;

    end if;
    return new;
end;
$$;

--
-- Name: trg_search_custom_filter_id; Type: TRIGGER; Schema: searchapp; Owner: -
--
CREATE TRIGGER trg_search_custom_filter_id BEFORE INSERT ON search_custom_filter FOR EACH ROW EXECUTE PROCEDURE tf_trg_search_custom_filter_id();

