--
-- Name: gwas_plink.plink_data; Type: TABLE; Schema: gwas_plink; Owner: -
--
CREATE TABLE gwas_plink.plink_data (
	plink_data_id integer,
	study_id      character varying(100) not null default nextval('gwas_plink.plink_data_plink_data_id_seq'::regclass),
	bed           oid,
	bim           oid,
	fam           oid
);

ALTER TABLE gwas_plink.plink_data
        ADD PRIMARY KEY plink_data_pkey (plink_data_id);

CREATE UNIQUE INDEX plink_data_study_id_key ON gwas_plink.plink_data(study_id);
