--
-- Name: ctd2_study; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE ctd2_study (
    ctd_study_id int,
    ref_article_protocol_id character varying(1000),
    reference_id int NOT NULL,
    pubmed_id character varying(250),
    pubmed_title character varying(2000),
    protocol_id character varying(1000),
    protocol_title character varying(2000)
);

--
-- Name: tf_trg_ctd2_study_id(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_ctd2_study_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin     
    if new.ctd_study_id is null then 
	select nextval('biomart.seq_clinical_trial_design_id') into new.ctd_study_id ;  
    end if;    
    return new;
end;
$$;

--
-- Name: trg_ctd2_study_id; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_ctd2_study_id BEFORE INSERT ON ctd2_study FOR EACH ROW EXECUTE PROCEDURE tf_trg_ctd2_study_id();

