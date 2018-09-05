--
-- deapp.de_gene_info
-- primary key chrom
-- unique key gene_source_id, gene_symbol
--

set search_path = deapp, pg_catalog;

ALTER TABLE IF EXISTS deapp.de_gene_info ALTER COLUMN chrom SET NOT NULL;

ALTER TABLE IF EXISTS ONLY deapp.de_gene_info
    ADD CONSTRAINT de_gene_info_pk PRIMARY KEY (gene_info_id);

ALTER TABLE IF EXISTS ONLY deapp.de_gene_info
    ADD CONSTRAINT de_gene_info_uk UNIQUE (gene_source_id,gene_symbol);

ALTER INDEX de_gene_info_pk SET TABLESPACE indx;
ALTER INDEX de_gene_info_uk SET TABLESPACE indx;

