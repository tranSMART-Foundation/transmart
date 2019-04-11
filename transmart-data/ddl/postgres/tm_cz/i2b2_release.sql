--
-- Name: i2b2_release; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE i2b2_release (
    c_hlevel int,
    c_fullname character varying(900) NOT NULL,
    c_name character varying(2000),
    c_synonym_cd character(1),
    c_visualattributes character(3),
    c_totalnum int,
    c_basecode character varying(450),
    c_metadataxml text,
    c_facttablecolumn character varying(50),
    c_tablename character varying(50),
    c_columnname character varying(50),
    c_columndatatype character varying(50),
    c_operator character varying(10),
    c_dimcode character varying(900),
    c_comment text,
    c_tooltip character varying(900),
    update_date timestamp,
    download_date timestamp,
    import_date timestamp,
    sourcesystem_cd character varying(50),
    valuetype_cd character varying(50),
    i2b2_id int,
    release_study character varying(50)
);

