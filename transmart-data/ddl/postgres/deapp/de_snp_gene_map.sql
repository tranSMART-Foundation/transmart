--
-- Name: de_snp_gene_map; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_snp_gene_map (
    snp_id int,
    snp_name character varying(255),
    entrez_gene_id int,
    entrez_gene_name character varying(255)
);
--
-- Name: snp_name_idx1; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX snp_name_idx1 ON de_snp_gene_map USING btree (snp_name);
--
-- Name: entrez_idx1; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX entrez_idx1 ON de_snp_gene_map USING btree (entrez_gene_id);
--
-- Name: fk_snp_gene_map_snp_id; Type: FK CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE de_snp_gene_map
    ADD CONSTRAINT fk_snp_gene_map_snp_id FOREIGN KEY (snp_id) REFERENCES de_snp_info(snp_info_id);
