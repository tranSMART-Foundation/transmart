--
-- Name: search_custom_filter_item; Type: TABLE; Schema: searchapp; Owner: -
--
CREATE TABLE search_custom_filter_item (
    search_custom_filter_item_id int NOT NULL,
    search_custom_filter_id int NOT NULL,
    unique_id character varying(200) NOT NULL,
    bio_data_type character varying(100) NOT NULL
);

--
-- Name: search_cust_fil_item_pk; Type: CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY search_custom_filter_item
    ADD CONSTRAINT search_cust_fil_item_pk PRIMARY KEY (search_custom_filter_item_id);

--
-- Name: tf_trg_search_cust_fil_item_id(); Type: FUNCTION; Schema: searchapp; Owner: -
--
CREATE FUNCTION tf_trg_search_cust_fil_item_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin 
    if new.search_custom_filter_item_id is null then
        select nextval('searchapp.seq_search_data_id') into new.search_custom_filter_item_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_search_cust_fil_item_id; Type: TRIGGER; Schema: searchapp; Owner: -
--
CREATE TRIGGER trg_search_cust_fil_item_id BEFORE INSERT ON search_custom_filter_item FOR EACH ROW EXECUTE PROCEDURE tf_trg_search_cust_fil_item_id();

