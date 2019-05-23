set search_path = amapp, pg_catalog;
--
-- Name: am_tag_value; Type: TABLE; Schema: amapp; Owner: -
--
CREATE TABLE am_tag_value (
    tag_value_id int NOT NULL,
    value character varying(2000)
);

--
-- Name: am_tag_value_pk; Type: CONSTRAINT; Schema: amapp; Owner: -
--
ALTER TABLE ONLY am_tag_value
    ADD CONSTRAINT am_tag_value_pk PRIMARY KEY (tag_value_id);

--
-- Name: tf_trg_am_tag_value_id(); Type: FUNCTION; Schema: amapp; Owner: -
--
CREATE FUNCTION tf_trg_am_tag_value_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.tag_value_id is null then
	select nextval('amapp.seq_amapp_data_id') into new.tag_value_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_am_tag_value_id; Type: TRIGGER; Schema: amapp; Owner: -
--
CREATE TRIGGER trg_am_tag_value_id BEFORE INSERT ON am_tag_value FOR EACH ROW EXECUTE PROCEDURE tf_trg_am_tag_value_id();

--
-- Name: tf_trg_am_tag_value_uid(); Type: FUNCTION; Schema: amapp; Owner: -
--
CREATE FUNCTION tf_trg_am_tag_value_uid() RETURNS trigger
    LANGUAGE plpgsql
AS $$
    declare
    rec_count int;
begin
    select count(*) into rec_count 
      from amapp.am_data_uid 
     where am_data_id = new.tag_value_id;

    if (rec_count = 0) then
	insert into amapp.am_data_uid (am_data_id, unique_id, am_data_type)
	values (new.tag_value_id, amapp.am_tag_value_uid(new.tag_value_id), 'AM_TAG_VALUE');
    end if;
    return new;
end;
$$;


SET default_with_oids = false;

--
-- Name: trg_am_tag_value_uid; Type: TRIGGER; Schema: amapp; Owner: -
--
CREATE TRIGGER trg_am_tag_value_uid AFTER INSERT ON am_tag_value FOR EACH ROW EXECUTE PROCEDURE tf_trg_am_tag_value_uid();

