--
-- Name: i2b2_load_path_with_count; Type: TABLE; Schema: tm_wz; Owner: -
--
CREATE TABLE i2b2_load_path_with_count (
    c_fullname character varying(700),
    nbr_children int,
    PRIMARY KEY (c_fullname)
);
