--
-- Name: plink_data; Type: TABLE; Schema: gwas_plink; Owner: -
--
CREATE TABLE plink_data (
	plink_data_id integer,
	study_id      character varying(50) default nextval('seq_plink_data_id'::regclass) not null,
	bed           oid,
	bim           oid,
	fam           oid
);

ALTER TABLE plink_data
        ADD PRIMARY KEY plink_data_pkey (plink_data_id);

CREATE UNIQUE INDEX plink_data_study_id_key ON plink_data(study_id);
