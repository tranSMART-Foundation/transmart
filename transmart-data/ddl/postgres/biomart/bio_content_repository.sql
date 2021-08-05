--
-- Name: bio_content_repository; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_content_repository (
    bio_content_repo_id int NOT NULL,
    location character varying(510),
    active_y_n character(1),
    repository_type character varying(200) NOT NULL,
    location_type character varying(200)
);

--
-- Name: bio_content_repository_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_content_repository
    ADD CONSTRAINT bio_content_repository_pk PRIMARY KEY (bio_content_repo_id);

--
-- Name: tf_trg_bio_content_repo_id(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_bio_content_repo_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.bio_content_repo_id is null then
        select nextval('biomart.seq_bio_data_id') into new.bio_content_repo_id ;
    end if;
    return new;
end;
$$;

--
-- Name: trg_bio_content_repo_id; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_bio_content_repo_id BEFORE INSERT ON bio_content_repository FOR EACH ROW EXECUTE PROCEDURE tf_trg_bio_content_repo_id();

