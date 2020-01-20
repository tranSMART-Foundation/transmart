--
-- Name: cms_section; Type: TABLE; Schema: biomart_user; Owner: -
--
CREATE TABLE cms_section (
    id int NOT NULL,
    name character varying(255) NOT NULL,
    instance_type character varying(255) NOT NULL,
    closure character varying(2000) NOT NULL
);

--
-- Name: cms_section_pk; Type: CONSTRAINT; Schema: biomart_user; Owner: -
--
ALTER TABLE ONLY cms_section
    ADD CONSTRAINT cms_section_pk PRIMARY KEY (id);
--
-- Name: cms_section_uk; Type: CONSTRAINT; Schema: biomart_user; Owner: -
--
ALTER TABLE ONLY cms_section
    ADD CONSTRAINT cms_section_uk UNIQUE (name, instance_type);


--
-- Name: tf_trg_cms_section_id(); Type: FUNCTION; Schema: biomart_user; Owner: -
--
CREATE FUNCTION tf_trg_cms_section_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.id is null then
        select nextval('biomart_user.cms_section_id_seq') into new.id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_cms_section_id; Type: TRIGGER; Schema: biomart_user; Owner: -
--
CREATE TRIGGER trg_cms_section_id BEFORE INSERT ON biomart_user.cms_section FOR EACH ROW EXECUTE PROCEDURE tf_trg_cms_section_id();
