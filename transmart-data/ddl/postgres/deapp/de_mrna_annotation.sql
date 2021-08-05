--
-- Name: de_mrna_annotation; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_mrna_annotation (
    gpl_id character varying(50),
    probe_id character varying(100),
    gene_symbol character varying(100),
    probeset_id int,
    gene_id int,
    organism character varying(100),
    de_mrna_annotation_id int NOT NULL
);

--
-- Name: de_mrna_annotation_pk; Type: CONSTRAINT; Schema: deapp; Owner: -
--
ALTER TABLE ONLY de_mrna_annotation
    ADD CONSTRAINT de_mrna_annotation_pk PRIMARY KEY (de_mrna_annotation_id);

--
-- Name: de_mrna_annotation_idx1; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_mrna_annotation_idx1 ON de_mrna_annotation USING btree (gpl_id, probe_id);

--
-- Name: de_mrna_annotation_idx2; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_mrna_annotation_idx2 ON de_mrna_annotation USING btree (gene_id, probeset_id);

--
-- Name: de_mrna_annotation_idx3; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX de_mrna_annotation_idx3 ON de_mrna_annotation USING btree (probeset_id);

--
-- Name: tf_trg_de_mrna_annotation_id(); Type: FUNCTION; Schema: deapp; Owner: -
--
CREATE FUNCTION tf_trg_de_mrna_annotation_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.de_mrna_annotation_id is null then
	select nextval('deapp.seq_de_mrna_annotation_id') into new.de_mrna_annotation_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_de_mrna_annotation_id; Type: TRIGGER; Schema: deapp; Owner: -
--
CREATE TRIGGER trg_de_mrna_annotation_id BEFORE INSERT ON de_mrna_annotation FOR EACH ROW EXECUTE PROCEDURE tf_trg_de_mrna_annotation_id();

--
-- Name: seq_de_mrna_annotation_id; Type: SEQUENCE; Schema: deapp; Owner: -
--
CREATE SEQUENCE seq_de_mrna_annotation_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

