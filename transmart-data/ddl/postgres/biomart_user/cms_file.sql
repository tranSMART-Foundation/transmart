--
-- Name: cms_file; Type: TABLE; Schema: biomart_user; Owner: -
--
CREATE TABLE cms_file (
    id int NOT NULL,
    name character varying(255) NOT NULL,
    content_type character varying(255) NOT NULL,
    instance_type character varying(255) NOT NULL,
    last_updated timestamp,
    version int NOT NULL,
    bytes bytea
);

--
-- Name: cms_file_pk; Type: CONSTRAINT; Schema: biomart_user; Owner: -
--
ALTER TABLE ONLY cms_file
    ADD CONSTRAINT cms_file_pk PRIMARY KEY (id);

--
-- Name: cms_file_uk; Type: CONSTRAINT; Schema: biomart_user; Owner: -
--
ALTER TABLE ONLY cms_file
    ADD CONSTRAINT cms_file_uk UNIQUE (name, instance_type);

--
-- Name: tf_trg_cms_file_id(); Type: FUNCTION; Schema: biomart_user; Owner: -
--
CREATE FUNCTION tf_trg_cms_file_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.id is null then
        select nextval('biomart_user.cms_file_id_seq') into new.id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_cms_file_id; Type: TRIGGER; Schema: biomart_user; Owner: -
--
CREATE TRIGGER trg_cms_file_id BEFORE INSERT ON biomart_user.cms_file FOR EACH ROW EXECUTE PROCEDURE tf_trg_cms_file_id();
