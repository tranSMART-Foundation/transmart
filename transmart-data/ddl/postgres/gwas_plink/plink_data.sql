--
-- Name: plink_data; Type: TABLE; Schema: gwas_plink; Owner: -
--
CREATE TABLE plink_data (
	plink_data_id int NOT NULL,
	study_id      character varying(50) not null,
	bed           oid,
	bim           oid,
	fam           oid
);

ALTER TABLE ONLY plink_data
        ADD CONSTRAINT plink_data_pkey PRIMARY KEY (plink_data_id);

ALTER TABLE ONLY plink_data
        ADD CONSTRAINT plink_data_study_id_key UNIQUE (study_id);
--
-- Name: tf_trg_plink_data_id(); Type: FUNCTION; Schema: gwas_plink; Owner: -
--
CREATE FUNCTION tf_trg_plink_data_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.plink_data_id is null then
        select nextval('gwas_plink.plink_data_id') into new.plink_data_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_plink_data_id; Type: TRIGGER; Schema: gwas_plink; Owner: -
--
CREATE TRIGGER trg_plink_data_id BEFORE INSERT ON plink_data FOR EACH ROW EXECUTE PROCEDURE tf_trg_plink_data_id();
