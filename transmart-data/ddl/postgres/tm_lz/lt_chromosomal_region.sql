--
-- Name: lt_chromo_region_id_seq; Type: SEQUENCE; Schema: tm_lz; Owner: -
--
CREATE SEQUENCE lt_chromo_region_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: lt_chromosomal_region; Type: TABLE; Schema: tm_lz; Owner: -
--
CREATE TABLE lt_chromosomal_region (
    region_id int DEFAULT nextval('lt_chromo_region_id_seq'::regclass) NOT NULL,
    gpl_id character varying(50),
    chromosome character varying(2),
    start_bp int,
    end_bp int,
    num_probes int,
    region_name character varying(100),
    cytoband character varying(100),
    gene_symbol character varying(100),
    gene_id int,
    organism character varying(100)
);

--
-- Name: lt_chromosomal_region_pk; Type: CONSTRAINT; Schema: tm_lz; Owner: -
--
ALTER TABLE ONLY lt_chromosomal_region
    ADD CONSTRAINT lt_chromosomal_region_pk PRIMARY KEY (region_id);

