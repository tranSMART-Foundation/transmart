--
-- Name: fm_file; Type: TABLE; Schema: fmapp; Owner: -
--
CREATE TABLE fm_file (
    file_id int NOT NULL,
    display_name character varying(1000) NOT NULL,
    original_name character varying(1000) NOT NULL,
    file_version int,
    file_type character varying(100),
    file_size int,
    filestore_location character varying(1000),
    filestore_name character varying(1000),
    link_url character varying(1000),
    active_ind boolean NOT NULL,
    create_date timestamp NOT NULL,
    update_date timestamp NOT NULL
);

--
-- Name: fm_file_pk; Type: CONSTRAINT; Schema: fmapp; Owner: -
--
ALTER TABLE ONLY fm_file
    ADD CONSTRAINT fm_file_pk PRIMARY KEY (file_id);

--
-- Name: tf_trg_fm_file_id(); Type: FUNCTION; Schema: fmapp; Owner: -
--
CREATE FUNCTION tf_trg_fm_file_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.file_id is null then
	select nextval('fmapp.seq_fm_id') into new.file_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_fm_file_id; Type: TRIGGER; Schema: fmapp; Owner: -
--
CREATE TRIGGER trg_fm_file_id BEFORE INSERT ON fm_file FOR EACH ROW EXECUTE PROCEDURE tf_trg_fm_file_id();

--
-- Name: tf_trg_fm_file_uid(); Type: FUNCTION; Schema: fmapp; Owner: -
--
CREATE FUNCTION tf_trg_fm_file_uid() RETURNS trigger
    LANGUAGE plpgsql
AS $$
    declare
    rec_count int;
begin
    select count(*) into rec_count 
      from fmapp.fm_data_uid 
     where fm_data_id = new.file_id;
    
    if rec_count = 0 then
	insert into fmapp.fm_data_uid (fm_data_id, unique_id, fm_data_type)
	values (new.file_id, fm_file_uid(new.file_id::text), 'FM_FILE');
    end if;
    return new;
end;
$$;

--
-- Name: trg_fm_file_uid; Type: TRIGGER; Schema: fmapp; Owner: -
--
CREATE TRIGGER trg_fm_file_uid BEFORE INSERT ON fm_file FOR EACH ROW EXECUTE PROCEDURE tf_trg_fm_file_uid();

--
-- Name: seq_fm_id; Type: SEQUENCE; Schema: fmapp; Owner: -
--
CREATE SEQUENCE seq_fm_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

