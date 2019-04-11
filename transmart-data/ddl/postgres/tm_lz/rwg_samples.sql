--
-- Name: rwg_samples; Type: TABLE; Schema: tm_lz; Owner: -
--
CREATE TABLE rwg_samples (
    study_id character varying(500),
    expr_id character varying(500),
    cohorts character varying(500),
    import_date timestamp DEFAULT now() NOT NULL
);

