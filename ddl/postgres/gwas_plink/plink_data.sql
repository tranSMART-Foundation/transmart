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

ALTER TABLE ONLY plink_data
        ADD CONSTRAINT plink_data_pkey PRIMARY KEY (plink_data_id);

ALTER TABLE ONLY plink_data
        ADD CONSTRAINT plink_data_study_id_key UNIQUE (study_id);
