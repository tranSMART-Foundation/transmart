--
-- Name: bio_ad_hoc_property; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_ad_hoc_property (
    ad_hoc_property_id int NOT NULL,
    bio_data_id int NOT NULL,
    property_key character varying(50),
    property_value character varying(2000)
);

--
-- Name: bio_ad_hoc_property_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_ad_hoc_property
    ADD CONSTRAINT bio_ad_hoc_property_pk PRIMARY KEY (ad_hoc_property_id);

--
-- Name: tf_trg_bio_ad_hoc_prop_id(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_bio_ad_hoc_prop_id() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
begin
    if new.ad_hoc_property_id is null then
	select nextval('biomart.seq_bio_data_id') into new.ad_hoc_property_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_bio_ad_hoc_prop_id; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_bio_ad_hoc_prop_id BEFORE INSERT ON bio_ad_hoc_property FOR EACH ROW EXECUTE PROCEDURE tf_trg_bio_ad_hoc_prop_id();

