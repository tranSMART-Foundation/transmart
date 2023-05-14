--
-- Name: am_tag_template_association; Type: TABLE; Schema: amapp; Owner: -
--
CREATE TABLE am_tag_template_association (
    tag_template_id int NOT NULL,
    object_uid character varying(300) NOT NULL,
    id int
);

--
-- Name: am_tag_template_association_pk; Type: CONSTRAINT; Schema: amapp; Owner: -
--
ALTER TABLE ONLY am_tag_template_association
    ADD CONSTRAINT am_tag_template_assoc_pk PRIMARY KEY (tag_template_id, object_uid);

--
-- Name: tf_trg_am_tag_temp_assoc_id(); Type: FUNCTION; Schema: amapp; Owner: -
--
CREATE FUNCTION tf_trg_am_tag_temp_assoc_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.id is null then
	select nextval('amapp.seq_amapp_data_id') into new.id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_am_tag_temp_assoc_id; Type: TRIGGER; Schema: amapp; Owner: -
--
CREATE TRIGGER trg_am_tag_temp_assoc_id BEFORE INSERT ON am_tag_template_association FOR EACH ROW EXECUTE PROCEDURE tf_trg_am_tag_temp_assoc_id();

