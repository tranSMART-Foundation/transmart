--
-- Name: ctd2_disease; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE ctd2_disease (
    ctd_disease_seq int,
    ctd_study_id int,
    disease_type_name character varying(500),
    disease_common_name character varying(500),
    icd10_name character varying(250),
    mesh_name character varying(250),
    study_type_name character varying(2000),
    physiology_name character varying(500)
);

--
-- Name: tf_trg_ctd2_disease(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_ctd2_disease() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin     
    if new.ctd_disease_seq is null then 
	select nextval('biomart.seq_clinical_trial_design_id') into new.ctd_disease_seq ;  
    end if;    
    return new;
end;
$$;

--
-- Name: trg_ctd2_disease; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_ctd2_disease BEFORE INSERT ON ctd2_disease FOR EACH ROW EXECUTE PROCEDURE tf_trg_ctd2_disease();

