--
-- Name: cz_data_profile_column_sample; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE cz_data_profile_column_sample (
    table_name character varying(500) NOT NULL,
    column_name character varying(500) NOT NULL,
    value character varying(4000),
    count int,
    etl_date timestamp DEFAULT CURRENT_TIMESTAMP
);

