--
-- Name: lt_src_metabolomic_data; Type: TABLE; Schema: tm_lz; Owner: -
--
CREATE TABLE lt_src_metabolomic_data (
    trial_name character varying(100),
    biochemical character varying(200),
    expr_id character varying(100),
    intensity_value character varying(50) --numeric when processed
);

