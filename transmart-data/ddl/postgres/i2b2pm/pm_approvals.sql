--
-- Name: pm_approvals; Type: TABLE; Schema: i2b2pm; Owner: -
--
CREATE TABLE pm_approvals (
    approval_id character varying(50) NOT NULL,
    approval_name character varying(255),
    approval_description character varying(2000),
    approval_activation_date timestamp,
    approval_expiration_date timestamp,
    object_cd character varying(50),
    change_date timestamp,
    entry_date timestamp,
    changeby_char character varying(50),
    status_cd character varying(50)
);
