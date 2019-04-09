--
-- Name: pm_project_request; Type: TABLE; Schema: i2b2pm; Owner: -
--
CREATE TABLE pm_project_request (
    id bigint,
    title character varying(255),
    request_xml text NOT NULL,
    change_date timestamp without time zone,
    entry_date timestamp without time zone,
    changeby_char character varying(50),
    status_cd character varying(50),
    project_id character varying(50),
    submit_char character varying(50)
);
--
-- Name: tf_trg_pm_project_request_inc(); Type: FUNCTION; Schema: i2b2pm; Owner: -
--
CREATE FUNCTION tf_trg_pm_project_request_inc() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
begin
       if NEW.ID is null then
 select nextval('i2b2pm.PM_PARAMS') into NEW.ID ;
end if;
       RETURN NEW;
end;
$$;
--
-- Name: pm_project_request_inc; Type: TRIGGER; Schema: i2b2pm; Owner: -
--
CREATE TRIGGER trg_pm_project_request_inc BEFORE INSERT ON pm_project_request FOR EACH ROW EXECUTE PROCEDURE tf_trg_pm_project_request_inc();
--
-- Name: pm_project_request_pk; Type: CONSTRAINT; Schema: i2b2pm; Owner: -
--
ALTER TABLE ONLY pm_project_request
    ADD CONSTRAINT pm_project_request_pk PRIMARY KEY (id);
