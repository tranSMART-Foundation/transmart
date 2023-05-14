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
    facet_id int
);

--
-- Name: faceted_search_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY faceted_search
    ADD CONSTRAINT faceted_search_pk PRIMARY KEY (facet_id);
