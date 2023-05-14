--
-- Name: de_pathway; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_pathway (
    name character varying(300),
    description character varying(510),
    id int NOT NULL,
    type character varying(100),
    source character varying(100),
    externalid character varying(100),
    pathway_uid character varying(200),
    user_id int
);

--
-- Name: de_pathway_pk; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_pathway
    ADD CONSTRAINT de_pathway_pk PRIMARY KEY (id);

--
-- Name: de_pathway_name_idx; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_pathway_name_idx ON de_pathway USING btree (name);

--
-- Name: de_pathway_idx3; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_pathway_idx3 ON de_pathway USING btree (pathway_uid, id);

--
-- Name: tf_trg_de_pathway_id(); Type: FUNCTION; Schema: deapp; Owner: -
--
CREATE FUNCTION tf_trg_de_pathway_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.id is null then
	select nextval('deapp.seq_data_id') into new.id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_de_pathway_id; Type: TRIGGER; Schema: deapp; Owner: -
--
CREATE TRIGGER trg_de_pathway_id BEFORE INSERT ON de_pathway FOR EACH ROW EXECUTE PROCEDURE tf_trg_de_pathway_id();

