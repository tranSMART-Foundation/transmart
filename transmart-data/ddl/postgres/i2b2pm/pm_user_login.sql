--
-- Name: pm_user_login; Type: TABLE; Schema: i2b2pm; Owner: -
--
CREATE TABLE pm_user_login (
    user_id character varying(50) NOT NULL,
    attempt_cd character varying(50) NOT NULL,
    entry_date timestamp NOT NULL,
    changeby_char character varying(50),
    status_cd character varying(50)
);
--
-- Name: pm_user_login_pk; Type: CONSTRAINT; Schema: i2b2pm; Owner: -
--
ALTER TABLE ONLY pm_user_login
    ADD CONSTRAINT pm_user_login_pk PRIMARY KEY (entry_date,user_id);
