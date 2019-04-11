--
-- Name: cz_data_profile_stats; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE cz_data_profile_stats (
    table_name character varying(500) NOT NULL,
    column_name character varying(500) NOT NULL,
    data_type character varying(500),
    column_length int,
    column_precision int,
    column_scale int NOT NULL,
    total_count int,
    percentage_null real,
    null_count int,
    non_null_count int,
    distinct_count int,
    max_length integer,
    min_length integer,
    first_value character varying(4000),
    last_value character varying(4000),
    max_length_value character varying(4000),
    min_length_value character varying(4000),
    etl_date timestamp DEFAULT CURRENT_TIMESTAMP
);

