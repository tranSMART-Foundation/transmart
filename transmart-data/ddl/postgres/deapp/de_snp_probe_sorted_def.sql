--
-- Name: de_snp_probe_sorted_def; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_snp_probe_sorted_def (
    snp_probe_sorted_def_id int NOT NULL,
    platform_name character varying(255),
    num_probe int,
    chrom character varying(16),
    probe_def text,
    snp_id_def text
);

--
-- Name: snp_probe_sorted_def_pk; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_snp_probe_sorted_def
    ADD CONSTRAINT snp_probe_sorted_def_pk PRIMARY KEY (snp_probe_sorted_def_id);

--
-- Name: tf_trg_de_snp_probe_sorted_def_id(); Type: FUNCTION; Schema: deapp; Owner: -
--
CREATE FUNCTION tf_trg_de_snp_probe_sorted_def_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.snp_probe_sorted_def_id is null then
        select nextval('deapp.seq_data_id') into new.snp_probe_sorted_def_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_de_snp_probe_sorted_def_id; Type: TRIGGER; Schema: deapp; Owner: -
--
CREATE TRIGGER trg_de_snp_probe_sorted_def_id BEFORE INSERT ON de_snp_probe_sorted_def FOR EACH ROW EXECUTE PROCEDURE tf_trg_de_snp_probe_sorted_def_id();

