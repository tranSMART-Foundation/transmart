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
-- Name: pm_user_login_idx; Type: INDEX; Schema: i2b2pm; Owner: -
--
CREATE INDEX pm_user_login_idx ON pm_user_login USING btree (user_id,entry_date);
