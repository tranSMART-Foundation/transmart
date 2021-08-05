--
-- Name: pm_user_data; Type: TABLE; Schema: i2b2pm; Owner: -
--
CREATE TABLE pm_user_data (
    user_id character varying(50) NOT NULL,
    full_name character varying(255),
    password character varying(255),
    email character varying(255),
    project_path character varying(255),
    change_date timestamp,
    entry_date timestamp,
    changeby_char character varying(50),
    status_cd character varying(50)
);
--
-- Name: pm_user_data_pk; Type: CONSTRAINT; Schema: i2b2pm; Owner: -
--
ALTER TABLE ONLY pm_user_data
    ADD CONSTRAINT pm_user_data_pk PRIMARY KEY (user_id);
