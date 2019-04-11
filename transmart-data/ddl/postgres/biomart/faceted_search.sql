--
-- Name: faceted_search; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE faceted_search (
    analysis_id int,
    study int,
    study_id int,
    disease character varying(510),
    analyses character varying(200),
    data_type character varying(50),
    platform character varying(20),
    observation character varying(200),
    facet_id int,
    PRIMARY KEY (facet_id)
);
