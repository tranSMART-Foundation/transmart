--
-- Name: pm_hive_params; Type: TABLE; Schema: i2b2pm; Owner: -
--
CREATE TABLE pm_hive_params (
    id int NOT NULL,
    datatype_cd character varying(50),
    domain_id character varying(50) NOT NULL,
    param_name_cd character varying(50) NOT NULL,
    value text,
    change_date timestamp,
    entry_date timestamp,
    changeby_char character varying(50),
    status_cd character varying(50)
);

--
-- Name: tf_pm_hive_params_inc(); Type: FUNCTION; Schema: i2b2pm; Owner: -
--
CREATE FUNCTION tf_pm_hive_params_inc() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.id is null then
	select nextval('i2b2pm.pm_params') into new.id ;
    end if;
    return new;
end;
$$;

--
-- Name: pm_hive_params_inc; Type: TRIGGER; Schema: i2b2pm; Owner: -
--
CREATE TRIGGER pm_hive_params_inc BEFORE INSERT ON pm_hive_params FOR EACH ROW EXECUTE PROCEDURE tf_pm_hive_params_inc();

--
-- Name: pm_hive_params_pk; Type: CONSTRAINT; Schema: i2b2pm; Owner: -
--
ALTER TABLE ONLY pm_hive_params
    ADD CONSTRAINT pm_hive_params_pk PRIMARY KEY (id);
