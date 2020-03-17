--
-- Name: genego_gene_map; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE genego_gene_map (
    gene_symbol character varying(100),
    gene_id character varying(100),
    bio_marker_id int NOT NULL
);

