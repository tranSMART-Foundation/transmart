--
-- Name: im_audit; Type: TABLE; Schema: i2b2imdata; Owner: -
--
CREATE TABLE im_audit (
    query_date timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    lcl_site character varying(50) NOT NULL,
    lcl_id character varying(200) NOT NULL,
    user_id character varying(50) NOT NULL,
    project_id character varying(50) NOT NULL,
    comments text
);
