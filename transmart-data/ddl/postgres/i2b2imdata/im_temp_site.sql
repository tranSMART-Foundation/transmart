--
-- Name: im_temp_site; Type: TABLE; Schema: i2b2imdata; Owner: -
--
CREATE TEMPORARY TABLE im_temp_site (
    lcl_site character varying(50),
    lcl_id character varying(200),
    project_id character varying(50)
) ON COMMIT PRESERVE ROWS;
