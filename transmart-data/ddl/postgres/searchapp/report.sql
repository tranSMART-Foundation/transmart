--
-- Name: report; Type: TABLE; Schema: searchapp; Owner: -
--
CREATE TABLE report (
    report_id int,
    name character varying(200),
    description character varying(1000),
    creatinguser character varying(200),
    public_flag character varying(20),
    create_date timestamp,
    study character varying(200)
);

