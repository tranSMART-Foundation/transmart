--
-- Name: protein_dbl_comp_qry(character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, refcursor); Type: FUNCTION; Schema: deapp; Owner: -
--
CREATE FUNCTION protein_dbl_comp_qry(patient_ids1 character varying, patient_ids2 character varying, sample_types1 character varying, sample_types2 character varying, pathway_uid1 character varying, pathway_uid2 character varying, timepoints1 character varying, timepoints2 character varying, INOUT cv_1 refcursor) RETURNS refcursor
    LANGUAGE plpgsql
AS $$
    DECLARE

    sample_record_count1 integer;
    sample_record_count2 integer;
    timepoint_count1 integer;
    timepoint_count2 integer;

BEGIN
    -- Check if sample Types Exist
    select count(*)
      into sample_record_count1
      from de_subject_sample_mapping
     where concept_code in
	--Passing string to Text parser Function
	   (select * from table(text_parser(sample_types1)));

    select count(*)
      into sample_record_count2
      from de_subject_sample_mapping
     where concept_code in
	--Passing string to Text parser Function
	   (select * from table(text_parser(sample_types2)));

    --Sample Record Count is invalid or non existent.
    if (sample_record_count1 = 0) then
	begin

	    select count(*) into timepoint_count1
	      from table(text_parser(timepoints1));

	    select count(*) into timepoint_count2
	      from table(text_parser(timepoints2));

	    if ((timepoint_count1=0) and (timepoint_count2=0)) then

		open cv_1 for
		    select distinct a.component
				    ,a.gene_symbol
				    ,a.zscore
				    ,'S1_' || a.patient_id as patient_id
				    ,a.assay_id
				    ,a.intensity
		      from de_subject_protein_data a
			   ,de_pathway_gene c
			   ,de_pathway p
		     where p.pathway_uid= pathway_uid1
		       and c.pathway_id= p.id
		       and a.gene_symbol = c.gene_symbol
		       and a.patient_id in (select * from table(text_parser(patient_ids1)))
		     union
		    select distinct a.component
				    ,a.gene_symbol
				    ,a.zscore
				    ,'S2_' || a.patient_ID as patient_id
				    ,a.assay_id
				    ,a.intensity
		      from de_subject_protein_data a
			   ,de_pathway_gene c
			   ,de_pathway p
		     where p.pathway_uid= pathway_uid1
		       and c.pathway_id= p.id
		       and a.gene_symbol = c.gene_symbol
		       and a.patient_id in (select * from table(text_parser(patient_ids2)))
		     order by gene_symbol, component, patient_id ;

	    elsif ((timepoint_count1>0) and (timepoint_count2=0)) then
		open cv_1 for
		    select distinct a.component
				    ,a.gene_symbol
				    ,a.zscore
				    ,'S1_' || a.patient_id as patient_id
				    ,a.assay_id
				    ,a.intensity
		      from de_subject_protein_data a
			   ,de_pathway_gene c
			   ,de_pathway p
			   ,de_subject_sample_mapping b
		     where p.pathway_uid= pathway_uid1
		       and c.pathway_id= p.id
                       and a.gene_symbol = c.gene_symbol
                       and a.patient_id in (select * from table(text_parser(patient_ids1)))
                       and b.timepoint_cd in (select * from table(text_parser(timepoints1)))
                       and a.patient_id=b.patient_id
		       and a.timepoint=b.timepoint
                       and a.assay_id=b.assay_id
		     union
		    select distinct a.component
				    ,a.gene_symbol
				    ,a.zscore
				    ,'S2_' || a.patient_id as patient_id
				    ,a.assay_id
				    ,a.intensity
		      from de_subject_protein_data a
			   ,de_pathway_gene c
			   ,de_pathway p
		     where p.pathway_uid= pathway_uid1
		       and c.pathway_id= p.id
                       and a.gene_symbol = c.gene_symbol
                       and a.patient_id in (select * from table(text_parser(patient_ids2)))
		     order by gene_symbol, component, patient_id ;

	    elsif ((timepoint_count1=0) and (timepoint_count2>0)) then
		open cv_1 for
		    select distinct a.component
				    ,a.gene_symbol
				    ,a.zscore
				    ,'S1_' || a.patient_ID as patient_id
				    ,a.assay_id
				    ,a.intensity
		      from de_subject_protein_data a
			   ,de_pathway_gene c
			   ,de_pathway p
		     where p.pathway_uid= pathway_uid1
		       and c.pathway_id= p.id
		       and a.gene_symbol = c.gene_symbol
                       and a.patient_id in (select * from table(text_parser(patient_ids1)))
		     union
		    select distinct a.component
				    ,a.gene_symbol
				    ,a.zscore
				    ,'S2_' || a.patient_id as patient_id
				    ,a.assay_id
				    ,a.intensity
		      from de_subject_protein_data a
			   ,de_pathway_gene c
			   ,de_pathway p
			   ,de_subject_sample_mapping b
		     where p.pathway_uid= pathway_uid1
		       and c.pathway_id= p.id
		       and a.gene_symbol = c.gene_symbol
		       and a.patient_id in (select * from table(text_parser(patient_ids2)))
                       and b.timepoint_cd in (select * from table(text_parser(timepoints2)))
                       and a.patient_id=b.patient_id
		       and a.timepoint=b.timepoint
                       and say_id=b.assay_id
		     order by gene_symbol, component, patient_id ;

	    else

		open cv_1 for
		    select distinct a.component
				    ,a.gene_symbol
				    ,a.zscore
				    ,'S1_' || a.patient_id as patient_id
				    ,a.assay_id
				    ,a.intensity
		      from de_subject_protein_data a
			   ,de_pathway_gene c
			   ,de_pathway p
			   ,de_subject_sample_mapping b
		     where p.pathway_uid= pathway_uid1
		       and c.pathway_id= p.id
                       and a.gene_symbol = c.gene_symbol
                       and a.patient_id in (select * from table(text_parser(patient_ids1)))
		       and b.timepoint_cd in (select * from table(text_parser(timepoints1)))
                       and a.patient_id=b.patient_id
		       and a.timepoint=b.timepoint
			   end a.assay_id=b.assay_id
		     union
		    select distinct a.component
				    ,a.gene_symbol
				    ,a.zscore
				    ,'S2_' || a.patient_id as patient_id
				    ,a.assay_id
				    ,a.intensity
		      from de_subject_protein_data a
			   ,de_pathway_gene c
			   ,de_pathway p
			   ,de_subject_sample_mapping b
		     where p.pathway_uid= pathway_uid1
		       and c.pathway_id= p.id
                       and a.gene_symbol = c.gene_symbol
                       and a.patient_id in (select * from table(text_parser(patient_ids2)))
                       and b.timepoint_cd in (select * from table(text_parser(timepoints2)))
                       and a.patient_id=b.patient_id
		       and a.timepoint=b.timepoint
                       and a.assay_id=b.assay_id
		     order by gene_symbol
			      ,component
			      ,patient_id;
	    end if;
        end;

	--else use all filters (If Subject is non existent or invalid, then return
    else
	begin

	    if ((timepoint_count1=0) and (timepoint_count2=0)) then
		open cv_1 for
		    select distinct a.component
				    ,a.gene_symbol
				    ,a.zscore
				    ,'S1_' || a.patient_id as patient_id
				    ,a.assay_id
				    ,a.intensity
		      from de_subject_protein_data a
			   ,de_pathway_gene c
			   ,de_pathway p
			   ,de_subject_sample_mapping b
		     where p.pathway_uid= pathway_uid1
		       and c.pathway_id= p.id
                       and a.gene_symbol = c.gene_symbol
                       and a.patient_id = b.patient_id
		       and a.assay_id = b.assay_id
                       and b.concept_code in (select * from table(text_parser(sample_types1)))
                       and a.patient_id in (select * from table(text_parser(patient_ids1)))
		     union
		    select distinct a.component
				    ,a.gene_symbol
				    ,a.zscore
				    ,'S2_' || a.patient_id as patient_id
				    ,a.assay_id
				    ,a.intensity
		      from de_subject_protein_data a
			   ,de_pathway_gene c
			   ,de_pathway p
			   ,de_subject_sample_mapping b
		     where p.pathway_uid= pathway_uid1
		       and c.pathway_id= p.id
                       and a.gene_symbol = c.gene_symbol
                       and a.patient_id = b.patient_id
		       and a.assay_id = b.assay_id
                       and b.concept_code in (select * from table(text_parser(sample_types2)))
                       and a.patient_id in (select * from table(text_parser(patient_ids2)))
		     order by gene_symbol
			      ,component
			      ,patient_id;
	    elsif ((timepoint_count1>0) and (timepoint_count2=0)) then
		open cv_1 for
		    select distinct a.component
				    ,a.gene_symbol
				    ,a.zscore
				    ,'S1_' || a.patient_id as patient_id
				    ,a.assay_id
				    ,a.intensity
		      from de_subject_protein_data a
			   ,de_pathway_gene c
			   ,de_pathway p
			   ,de_subject_sample_mapping b
		     where p.pathway_uid= pathway_uid1
		       and c.pathway_id= p.id
		       and a.gene_symbol = c.gene_symbol
		       and a.patient_id = b.patient_id
		       and a.assay_id = b.assay_id
		       and b.concept_code in (select * from table(text_parser(sample_types1)))
		       and a.patient_id in (select * from table(text_parser(patient_ids1)))
		       and b.timepoint_cd in (select * from table(text_parser(timepoints1)))
		       and a.patient_id=b.patient_id
		       and a.timepoint=b.timepoint
		     union
		    select distinct a.component
				    ,a.gene_symbol
				    ,a.zscore
				    ,'S2_' || a.patient_id as patient_id
				    ,a.assay_id
				    ,a.intensity
		      from de_subject_protein_data a
			   ,de_pathway_gene c
			   ,de_pathway p
			   ,de_subject_sample_mapping b
		     where p.pathway_uid= pathway_uid1
		       and c.pathway_id= p.id
		       and a.gene_symbol = c.gene_symbol
		       and a.patient_id = b.patient_id
		       and a.assay_id = b.assay_id
		       and b.concept_code in (select * from table(text_parser(sample_types2)))
		       and a.patient_id in (select * from table(text_parser(patient_ids2)))
		     order by gene_symbol
			      ,component
			      ,patient_id;
	    elsif ((timepoint_count1=0) and (timepoint_count2>0)) then
		open cv_1 for
		    select distinct a.component
				    ,a.gene_symbol
				    ,a.zscore
				    ,'S1_' || a.patient_id as patient_id
				    ,a.assay_id
				    ,a.intensity
		      from de_subject_protein_data a
			   ,de_pathway_gene c
			   ,de_pathway p
			   ,de_subject_sample_mapping b
		     where p.pathway_uid= pathway_uid1
		       and c.pathway_id= p.id
		       and a.gene_symbol = c.gene_symbol
		       and a.patient_id = b.patient_id
		       and a.assay_id = b.assay_id
		       and b.concept_code in (select * from table(text_parser(sample_types1)))
		       and a.patient_id in (select * from table(text_parser(patient_ids1)))
		     union
		    select distinct a.component
				    ,a.gene_symbol
				    ,a.zscore
				    ,'S2_' || a.patient_id as patient_id
				    ,a.assay_id
				    ,a.intensity
		      from de_subject_protein_data a
			   ,de_pathway_gene c
			   ,de_pathway p
			   ,de_subject_sample_mapping b
		     where p.pathway_uid = pathway_uid1
		       and c.pathway_id = p.id
		       and a.gene_symbol = c.gene_symbol
		       and a.patient_id = b.patient_id
		       and a.assay_id = b.assay_id
		       and b.concept_code in (select * from table(text_parser(sample_types2)))
		       and a.patient_id in (select * from table(text_parser(patient_ids2)))
		       and b.timepoint_cd in (select * from table(text_parser(timepoints2)))
		       and a.patient_id = b.patient_id
		       and a.timepoint = b.timepoint
		     order by gene_symbol
			      ,component
			      ,patient_id;
	    else

		open cv_1 for
		    select distinct a.component
				    ,a.gene_symbol
				    ,a.zscore
				    ,'S1_' || a.patient_id as patient_id
				    ,a.assay_id
				    ,a.intensity
		      from de_subject_protein_data a
			   ,de_pathway_gene c
			   ,de_pathway p
			   ,de_subject_sample_mapping b
		     where p.pathway_uid = pathway_uid1
		       and c.pathway_id= p.id
		       and a.gene_symbol = c.gene_symbol
		       and a.patient_id = b.patient_id
		       and a.assay_id = b.assay_id
		       and b.concept_code in (select * from table(text_parser(sample_types1)))
		       and a.patient_id in (select * from table(text_parser(patient_ids1)))
		       and b.timepoint_cd in (select * from table(text_parser(timepoints1)))
                       and a.patient_id = b.patient_id
		       and a.timepoint = b.timepoint
		     union
		    select distinct a.component
				    ,a.gene_symbol
				    ,a.zscore
				    ,'S2' || a.patient_id as patient_id
				    ,a.assay_id
				    ,a.intensity
		      from de_subject_protein_data a
			   ,de_pathway_gene c
			   ,de_pathway p
			   ,de_subject_sample_mapping b
		     where p.pathway_uid = pathway_uid1
		       and c.pathway_id = p.id
		       and a.gene_symbol = c.gene_symbol
		       and a.patient_id = b.patient_id
		       and a.assay_id = b.assay_id
		       and b.concept_code in (select * from table(text_parser(sample_types2)))
		       and a.patient_id in (select * from table(text_parser(patient_ids2)))
		       and b.timepoint_cd in (select * from table(text_parser(timepoints2)))
		       and a.patient_id = b.patient_id
		       and a.timepoint = b.timepoint
		     order by gene_symbol
			      ,component
			      ,patient_id;

	    end if;

	end;

    end if;

END;
$$;

