--
-- Name: bio_data_platform; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_data_platform (
    bio_data_id int NOT NULL,
    bio_assay_platform_id int NOT NULL,
    etl_source character varying(100)
);

