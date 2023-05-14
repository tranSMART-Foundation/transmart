--
-- Name: de_xtrial_parent_names; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_xtrial_parent_names (
    parent_cd int NOT NULL,
    across_path character varying(500),
    manually_created int
);

--
-- Name: dextpn_parent_node_u; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_xtrial_parent_names
    ADD CONSTRAINT dextpn_parent_node_u UNIQUE (across_path);

--
-- Name: de_xtrial_parent_names_pk; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_xtrial_parent_names
    ADD CONSTRAINT de_xtrial_parent_names_pk PRIMARY KEY (parent_cd);

--
-- Name: tf_de_parent_cd_trg(); Type: FUNCTION; Schema: deapp; Owner: -
--
CREATE FUNCTION tf_de_parent_cd_trg() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    select nextval('deapp.de_parent_cd_seq')
      into new.parent_cd;

    return new;
end;
$$;

--
-- Name: de_parent_cd_trg; Type: TRIGGER; Schema: deapp; Owner: -
--
CREATE TRIGGER de_parent_cd_trg BEFORE INSERT ON de_xtrial_parent_names FOR EACH ROW WHEN ((COALESCE((new.parent_cd)::text, ''::text) = ''::text)) EXECUTE PROCEDURE tf_de_parent_cd_trg();

--
-- Name: de_parent_cd_seq; Type: SEQUENCE; Schema: deapp; Owner: -
--
CREATE SEQUENCE de_parent_cd_seq
    START WITH 3801
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 20;

