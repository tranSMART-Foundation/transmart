--
-- Name: de_pathway_gene; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_pathway_gene (
    id int NOT NULL,
    pathway_id int,
    gene_symbol character varying(100),
    gene_id character varying(100)
);

--
-- Name: de_pathway_gene_pk; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_pathway_gene
    ADD CONSTRAINT de_pathway_gene_pk PRIMARY KEY (id);

--
-- Name: de_pathway_gene_index1; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_pathway_gene_index1 ON de_pathway_gene USING btree (pathway_id, gene_symbol);

--
-- Name: de_pathway_gene_pathway; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_pathway_gene_pathway ON de_pathway_gene USING btree (pathway_id);

--
-- Name: de_pathway_gene_idx4; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_pathway_gene_idx4 ON de_pathway_gene USING btree (gene_symbol);

--
-- Name: tf_trg_de_pathway_gene_id(); Type: FUNCTION; Schema: deapp; Owner: -
--
CREATE FUNCTION tf_trg_de_pathway_gene_id() RETURNS trigger
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
-- Name: trg_de_pathway_gene_id; Type: TRIGGER; Schema: deapp; Owner: -
--
CREATE TRIGGER trg_de_pathway_gene_id BEFORE INSERT ON de_pathway_gene FOR EACH ROW EXECUTE PROCEDURE tf_trg_de_pathway_gene_id();

--
-- Name: seq_data_id; Type: SEQUENCE; Schema: deapp; Owner: -
--
CREATE SEQUENCE seq_data_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 20;

