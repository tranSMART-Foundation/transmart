set search_path = searchapp, pg_catalog;
--
-- Name: search_required_upload_field; Type: TABLE; Schema: searchapp; Owner: -
--
CREATE TABLE search_required_upload_field (
    required_upload_field_id int NOT NULL,
    type character varying(50),
    field character varying(50)
);

--
-- Name: search_reqd_upload_field_pk; Type: CONSTRAINT; Schema: search; Owner: -
--
ALTER TABLE ONLY search_required_upload_field
    ADD CONSTRAINT search_reqd_up_field_pk PRIMARY KEY (required_upload_field_id);

--
-- Name: tf_trg_srch_req_up_field_id(); Type: FUNCTION; Schema: searchapp; Owner: -
--
CREATE FUNCTION tf_trg_srch_req_up_field_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.required_upload_field_id is null then
	select nextval('searchapp.seq_req_upload_field_id') into new.required_upload_field_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_srch_req_up_field_id; Type: TRIGGER; Schema: searchapp; Owner: -
--
CREATE TRIGGER trg_srch_req_up_field_id BEFORE INSERT ON search_required_upload_field FOR EACH ROW EXECUTE PROCEDURE tf_trg_srch_req_up_field_id();

--
-- Name: seq_req_upload_field_id; Type: SEQUENCE; Schema: searchapp; Owner: -
--
CREATE SEQUENCE seq_req_upload_field_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 20;

