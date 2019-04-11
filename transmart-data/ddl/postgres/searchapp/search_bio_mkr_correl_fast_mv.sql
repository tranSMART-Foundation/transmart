--
-- Name: search_bio_mkr_correl_fast_mv; Type: TABLE; Schema: searchapp; Owner: -
--
CREATE TABLE search_bio_mkr_correl_fast_mv (
    domain_object_id int NOT NULL,
    asso_bio_marker_id int,
    correl_type character varying(19),
    value_metric int,
    mv_id int
);

