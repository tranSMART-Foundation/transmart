--
-- Name: de_gene_info; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_gene_info (
    gene_info_id serial NOT NULL,
    gene_source_id int DEFAULT 1 NOT NULL,
    entrez_id int,
    gene_symbol character varying(255) NOT NULL,
    gene_name character varying(255),
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
-- Name: de_gene_info_entrez_id_gene_source_id_idx; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_gene_info_entrez_id_gene_source_id_idx ON de_gene_info USING btree (entrez_id, gene_source_id);

--
-- Name: de_gene_info_gene_symbol_idx; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_gene_info_gene_symbol_idx ON de_gene_info USING btree (gene_symbol);

