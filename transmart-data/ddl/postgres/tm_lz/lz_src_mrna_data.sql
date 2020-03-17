--
-- Name: lz_src_mrna_data; Type: TABLE; Schema: tm_lz; Owner: -
--
CREATE TABLE lz_src_mrna_data (
    trial_name character varying(100),
    probeset character varying(100),
    expr_id character varying(100),
    intensity_value character varying(50), --numeric when processed
    source_cd character varying(200)
);

