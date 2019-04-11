--
-- Name: subset; Type: TABLE; Schema: searchapp; Owner: -
--
CREATE TABLE subset (
    subset_id int NOT NULL,
    description character varying(1000) NOT NULL,
    create_date timestamp NOT NULL,
    creating_user character varying(200) NOT NULL,
    query_master_id_1 int NOT NULL,
    query_master_id_2 int,
    study character varying(200),
    public_flag boolean NOT NULL,
    deleted_flag boolean NOT NULL
);

