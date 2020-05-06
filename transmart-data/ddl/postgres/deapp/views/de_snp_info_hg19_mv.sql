CREATE MATERIALIZED VIEW deapp.de_snp_info_hg19_mv AS
    SELECT rs_id
	   , chrom
	   , pos
	   , strand
	   , gene_name AS rsgene
	   , exon_intron
	   , recombination_rate
	   , regulome_score
      FROM deapp.de_rc_snp_info info
     WHERE info.hg_version='19';
