--
-- Name: pm_project_user_roles; Type: TABLE; Schema: i2b2pm; Owner: -
--
CREATE TABLE pm_project_user_roles (
    project_id character varying(50) NOT NULL,
    user_id character varying(50) NOT NULL,
    user_role_cd character varying(255) NOT NULL,
    change_date timestamp,
    entry_date timestamp,
    changeby_char character varying(50),
    status_cd character varying(50)
);
--
-- Name: pm_project_user_roles_pk; Type: CONSTRAINT; Schema: i2b2pm; Owner: -
--
ALTER TABLE ONLY pm_project_user_roles
    ADD CONSTRAINT pm_project_user_roles_pk PRIMARY KEY (project_id,user_id,user_role_cd);
