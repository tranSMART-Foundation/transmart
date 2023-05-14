--
-- Name: fm_folder; Type: TABLE; Schema: fmapp; Owner: -
--
CREATE TABLE fm_folder (
    folder_id int NOT NULL,
    folder_name character varying(1000) NOT NULL,
    folder_full_name character varying(1000) NOT NULL,
    folder_level int NOT NULL,
    folder_type character varying(100) NOT NULL,
    folder_tag character varying(50),
    active_ind boolean NOT NULL,
    parent_id int,
    description character varying(4000)
);

--
-- Name: fm_folder_pk; Type: CONSTRAINT; Schema: fmapp; Owner: -
--
ALTER TABLE ONLY fm_folder
    ADD CONSTRAINT fm_folder_pk PRIMARY KEY (folder_id);

--
-- Name: tf_trg_fm_folder_id(); Type: FUNCTION; Schema: fmapp; Owner: -
--
CREATE FUNCTION tf_trg_fm_folder_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if coalesce(new.folder_id::text, '') = '' then
        select nextval('fmapp.seq_fm_id') into new.folder_id ;
    end if;
    if coalesce(new.folder_full_name::text, '') = '' then
	if coalesce(new.parent_id::text, '') = '' then
	    select '\' || fm_folder_uid(new.folder_id) || '\' into new.folder_full_name ;
	else
	    select folder_full_name || fm_folder_uid(new.folder_id) || '\' into new.folder_full_name 
	      from fmapp.fm_folder
	     where folder_id = new.parent_id;
	end if;
    end if;
    return new;
end;
$$;

--
-- Name: trg_fm_folder_id; Type: TRIGGER; Schema: fmapp; Owner: -
--
CREATE TRIGGER trg_fm_folder_id BEFORE INSERT ON fm_folder FOR EACH ROW EXECUTE PROCEDURE tf_trg_fm_folder_id();

--
-- Name: tf_trg_fm_folder_uid(); Type: FUNCTION; Schema: fmapp; Owner: -
--
CREATE FUNCTION tf_trg_fm_folder_uid() RETURNS trigger
    LANGUAGE plpgsql
AS $$
    declare
    rec_count int;
begin
    select count(*) into rec_count 
      from fmapp.fm_data_uid 
     where fm_data_id = new.folder_id;
    
    if rec_count = 0 then
	insert into fmapp.fm_data_uid (fm_data_id, unique_id, fm_data_type)
	values (new.folder_id, fm_folder_uid(new.folder_id), 'FM_FOLDER');
    end if;
    return new;
end;
$$;


SET default_with_oids = false;

--
-- Name: trg_fm_folder_uid; Type: TRIGGER; Schema: fmapp; Owner: -
--
CREATE TRIGGER trg_fm_folder_uid BEFORE INSERT ON fm_folder FOR EACH ROW EXECUTE PROCEDURE tf_trg_fm_folder_uid();

