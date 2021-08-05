--
-- Name: pm_approvals_params; Type: TABLE; Schema: i2b2pm; Owner: -
--
CREATE TABLE pm_approvals_params (
    id int NOT NULL,
    approval_id character varying(50) NOT NULL,
    param_name_cd character varying(50) NOT NULL,
    value text,
    activation_date timestamp,
    expiration_date timestamp,
    datatype_cd character varying(50),
    object_cd character varying(50),
    change_date timestamp,
    entry_date timestamp,
    changeby_char character varying(50),
    status_cd character varying(50)
);
--
-- Name: pm_approval_params_pk; Type: CONSTRAINT; Schema: i2b2pm; Owner: -
--
ALTER TABLE ONLY pm_approvals_params
    ADD CONSTRAINT pm_approvals_params_pk PRIMARY KEY (id);
