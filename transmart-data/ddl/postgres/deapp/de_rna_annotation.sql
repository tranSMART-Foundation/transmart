--
-- Name: de_rna_annotation; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_rna_annotation (
    gpl_id character varying(50),
    transcript_id character varying(50),
    gene_symbol character varying(100),
    gene_id character varying(100),
    organism character varying(100),
    probeset_id int
);
