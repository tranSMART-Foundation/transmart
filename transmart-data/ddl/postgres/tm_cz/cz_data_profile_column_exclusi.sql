--
-- Name: cz_data_profile_column_exclusi; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE cz_data_profile_column_exclusi (
    table_name character varying(500) NOT NULL,
    column_name character varying(500) NOT NULL,
    exclusion_reason character varying(2000),
    etl_date timestamp DEFAULT CURRENT_TIMESTAMP
);

