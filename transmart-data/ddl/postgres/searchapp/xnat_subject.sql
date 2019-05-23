--
-- Name: xnat_subject; Type: TABLE; Schema: searchapp; Owner: -
--
CREATE TABLE xnat_subject
(
    tsmart_subjectid character varying(100),
    xnat_subjectid character varying(100),
    xnat_project character varying(80),
    id int NOT NULL
);

--
-- Name: xnat_subject_pk; Type: CONSTRAINT; Schema: searchapp; Owner: -
--
ALTER TABLE ONLY xnat_subject
    ADD CONSTRAINT xnat_subject_pk PRIMARY KEY (id);

--
-- Name: tf_trg_xnat_subject_id(); Type: FUNCTION; Schema: searchapp; Owner: -
--
CREATE FUNCTION tf_trg_xnat_subject_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.id is null then
        select nextval('searchapp.seq_search_data_id') into new.id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_xnat_subject_id; Type: TRIGGER; Schema: searchapp; Owner: -
--
CREATE TRIGGER trg_xnat_subject_id BEFORE INSERT ON xnat_subject FOR EACH ROW EXECUTE PROCEDURE tf_trg_xnat_subject_id();

