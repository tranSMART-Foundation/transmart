--
-- Name: protein_comparison_qry(character varying, character varying, character varying, character varying, refcursor); Type: FUNCTION; Schema: deapp; Owner: -
--
CREATE FUNCTION protein_comparison_qry(patient_ids character varying, sample_types character varying, pathway_uid1 character varying, timepoints character varying, INOUT cv_1 refcursor) RETURNS refcursor
    LANGUAGE plpgsql
AS $$
    DECLARE

    --Counter to check if samples exist.
    sample_record_count integer;
    timepoint_count integer;

BEGIN
    -------------------------------------------------------------------------------
    -- Returns PROBESET, GENE_SYMBOL, REFSEQ, LOG10_INTENSITY, PVALUE, PATIENT_ID, ASSAY_ID
    -- result set for specifed Pathways filtered
    -- by Sample Types and Subject_id.
    -- KCR@20090206 - First rev.
    -- KCR@20090212 - Second rev.  Changed logic so that if Sampel is not found it returns Zero.
    -- KCR@20090206 - Third rev. Using Collections to hold parsed values instead of DB table.
    -- HX@20090317  - Replace pathway_name with pathway_uid
    -- HX@20090318  - Add pathway_uid column into DE_PATHWAY and populate data
    --                from BIOMART.BIO_DATA_UID

    -- 2009-05-04: replace refseq with probeset
    -- 2009-05-26: remove GENE_SYMBOL's concatenation and change probeset to refseq
    -- 2009-05-29: change LOG10_INTENSITY to LOG2_INTENSITY
    -- 2009-06-18: add raw_intensity and change log2_intensity to zscore
    -- 2009-06-23: Add timepoints as a parameter
    -------------------------------------------------------------------------------

    -- Check if sample Types Exist
    select COUNT(*)
      into sample_record_count
      from DE_SUBJECT_SAMPLE_MAPPING
     where concept_code in
	--Passing string to Text parser Function
	   (select * from table(text_parser(sample_types)));

    --Sample Record Count is invalid or non existent.
    if sample_record_count = 0 then
	begin

	    select count(*) into timepoint_count
	      from table(text_parser(timepoints));

	    if timepoint_count=0 then

		open cv_1 for
			      select distinct a.component, a.GENE_SYMBOL, a.zscore,
			      a.patient_ID, a.ASSAY_ID, a.intensity
			      from DE_SUBJECT_PROTEIN_DATA a, DE_pathway_gene c, de_pathway p
			      where p.pathway_uid= pathway_uid1 and c.pathway_id= p.id and
			      a.gene_symbol = c.gene_symbol and
			      a.patient_id in (select * from table(text_parser(patient_ids)))
			      order by a.GENE_SYMBOL, a.COMPONENT, a.patient_ID ;

	    else

		open cv_1 for
			      select distinct a.COMPONENT, a.GENE_SYMBOL, a.zscore,
			      a.patient_ID, a.ASSAY_ID, a.intensity
			      from DE_SUBJECT_PROTEIN_DATA a, DE_pathway_gene c, de_pathway p,
			      DE_subject_sample_mapping b
			      where p.pathway_uid= pathway_uid1 and c.pathway_id= p.id and
			      a.gene_symbol = c.gene_symbol and
			      a.patient_id in (select * from table(text_parser(patient_ids))) and
			      b.TIMEPOINT_CD in (select * from table(text_parser(timepoints))) and
			      a.PATIENT_ID=b.patient_id and a.timepoint=b.timepoint and
			      a.assay_id=b.assay_id
			      order by a.GENE_SYMBOL, a.COMPONENT, a.patient_ID;
	    end if;
	end;

	--else use all filters (If Subject is non existent or invalid, then return
    else
	begin

	    if timepoint_count=0 then

		open cv_1 for
		    select distinct a.component
				    ,a.gene_symbol
				    ,a.zscore
				    ,a.patient_id
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
		       and b.concept_code in (select * from table(text_parser(sample_types)))
		       and a.patient_id in (select * from table(text_parser(patient_ids)))
		     order by a.gene_symbol, a.component, a.patient_id;

	    else

		open cv_1 for
		    select distinct a.component
				    ,a.gene_symbol
				    ,a.zscore
				    ,a.patient_id
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
		       and b.concept_code in (select * from table(text_parser(sample_types)))
		       and a.patient_id in (select * from table(text_parser(patient_ids)))
		       and b.timepoint_cd in (select * from table(text_parser(timepoints))) 
		       and a.patient_id=b.patient_id and a.timepoint=b.timepoint
		     order by a.gene_symbol, a.component, a.patientl_id;

	    end if;

	end;
    end if;
END;
$$;

