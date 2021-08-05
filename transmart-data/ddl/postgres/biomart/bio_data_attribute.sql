--
-- Name: bio_data_attribute; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_data_attribute (
    bio_data_attribute_id int NOT NULL,
    property_code character varying(200) NOT NULL,
    property_value character varying(200),
    bio_data_id int NOT NULL,
    property_unit character varying(100)
);

--
-- Name: bio_data_attr_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_data_attribute
    ADD CONSTRAINT bio_data_attr_pk PRIMARY KEY (bio_data_attribute_id);

--
-- Name: tf_trg_bio_data_attr_id(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_bio_data_attr_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.bio_data_attribute_id is null then
        select nextval('biomart.seq_bio_data_id') into new.bio_data_attribute_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_bio_data_attr_id; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_bio_data_attr_id BEFORE INSERT ON bio_data_attribute FOR EACH ROW EXECUTE PROCEDURE tf_trg_bio_data_attr_id();

