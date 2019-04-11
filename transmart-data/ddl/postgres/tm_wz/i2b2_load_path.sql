--
-- Name: i2b2_load_path; Type: TABLE; Schema: tm_wz; Owner: -
--
CREATE TABLE i2b2_load_path (
    path character varying(700),
    record_id char(10),
    PRIMARY KEY (path, record_id)
);
