--
-- Name: pm_user_session; Type: TABLE; Schema: i2b2pm; Owner: -
--
CREATE TABLE pm_user_session (
    user_id character varying(50) NOT NULL,
    session_id character varying(50) NOT NULL,
    expired_date timestamp,
    change_date timestamp,
    entry_date timestamp,
    changeby_char character varying(50),
    status_cd character varying(50)
);
--
-- Name: pm_user_session_pk; Type: CONSTRAINT; Schema: i2b2pm; Owner: -
--
ALTER TABLE ONLY pm_user_session
    ADD CONSTRAINT pm_user_session_pk PRIMARY KEY (session_id,user_id);
