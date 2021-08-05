--
-- Name: de_gene_info; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_gene_info (
    gene_info_id int NOT NULL,
    gene_source_id int DEFAULT 1 NOT NULL,
    entrez_id int,
    gene_symbol character varying(100) NOT NULL,
    gene_name character varying(100),
    chrom character varying(40) NOT NULL,
    chrom_start int,
    chrom_stop int,
    strand int
);

--
-- Name: de_gene_info_source_fk; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE de_gene_info
    ADD CONSTRAINT gene_info_source_fk FOREIGN KEY (gene_source_id) REFERENCES de_gene_source(gene_source_id) ON DELETE CASCADE;
--
-- Name: de_gene_info_pk; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_gene_info
    ADD CONSTRAINT de_gene_info_pk PRIMARY KEY (gene_info_id);

--
-- Name: de_gene_info_uk; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_gene_info
    ADD CONSTRAINT de_gene_info_uk UNIQUE (gene_source_id,gene_symbol);
--
-- Name: de_gi_eid_gs_id_idx; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_gi_eid_gs_id_idx ON de_gene_info USING btree (entrez_id, gene_source_id);

--
-- Name: de_gi_gs_idx; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_gi_gs_idx ON de_gene_info USING btree (gene_symbol);

--
-- Name: tf_trg_de_gene_info_id(); Type: FUNCTION; Schema: deapp; Owner: -
--
CREATE FUNCTION tf_trg_de_gene_info_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.gene_info_id is null then
        select nextval('deapp.seq_data_id') into new.gene_info_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_de_gene_info_id; Type: TRIGGER; Schema: deapp; Owner: -
--
CREATE TRIGGER trg_de_gene_info_id BEFORE INSERT ON de_gene_info FOR EACH ROW EXECUTE PROCEDURE tf_trg_de_gene_info_id();
