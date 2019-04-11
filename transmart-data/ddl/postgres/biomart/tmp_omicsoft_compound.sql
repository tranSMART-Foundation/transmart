--
-- Name: tmp_omicsoft_compound; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE tmp_omicsoft_compound (
    accession character varying(100),
    bio_compound_id int NOT NULL,
    cas_registry character varying(400),
    compound character varying(4000),
    bio_experiment_id int
);

