--
-- Name: ctd2_inhib_details; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE ctd2_inhib_details (
    ctd_inhib_seq int,
    ctd_study_id int,
    common_name_name character varying(500),
    standard_name_name character varying(500),
    experimental_detail_dose character varying(4000),
    exp_detail_exposure_period character varying(4000),
    exp_detail_treatment_name character varying(4000),
    exp_detail_admin_route character varying(4000),
    exp_detail_description character varying(4000),
    exp_detail_placebo character varying(250),
    comparator_name_name character varying(250),
    comp_treatment_name character varying(4000),
    comp_admin_route character varying(4000),
    comp_dose character varying(2000),
    comp_exposure_period character varying(2000)
);

--
-- Name: tf_trg_ctd2_inhib_details(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_ctd2_inhib_details() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin     
    if new.ctd_inhib_seq is null then 
	select nextval('biomart.seq_clinical_trial_design_id') into new.ctd_inhib_seq ;  
    end if;    
    return new;
end;
$$;

--
-- Name: trg_ctd2_inhib_details; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_ctd2_inhib_details BEFORE INSERT ON ctd2_inhib_details FOR EACH ROW EXECUTE PROCEDURE tf_trg_ctd2_inhib_details();

