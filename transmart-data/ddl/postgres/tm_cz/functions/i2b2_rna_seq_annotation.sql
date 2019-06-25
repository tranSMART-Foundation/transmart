--
-- Name: i2b2_rna_seq_annotation(); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION i2b2_rna_seq_annotation() RETURNS bigint
    LANGUAGE plpgsql
AS $$
    declare

    gpl_rtn bigint;
    newJobFlag numeric(1);
    databaseName character varying(100);
    procedureName character varying(100);
    jobID bigint;
    errorNumber		character varying;
    errorMessage	character varying;
    rowCt			numeric(18,0);
    stepCt bigint;

begin

    stepCt := 0;

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := -1;
    
    databaseName := 'tm_cz';
    procedureName := 'i2b2_rna_seq_annotation';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(coalesce(jobID::text, '') = '' or jobID < 1) then
	newJobFlag := 1; -- True
	select cz_start_audit (procedureName, databaseName, jobID) into jobId;
    end if;

    stepCt := stepCt + 1;
    perform cz_write_audit(jobId,databaseName,procedureName,'Starting i2b2_rna_seq_annotation',0,stepCt,'Done');
    
    -- insert into deapp.de_rnaseq_annotation
    
    select count(platform) into gpl_rtn
      from deapp.de_gpl_info
     where marker_type='RNASEQ'
       and (platform is NOT NULL
	    and platform::text <> '');
    if gpl_rtn=0 then
	perform cz_write_audit(jobId,databasename,procedurename,'Platform data missing from DEAPP.DE_GPL_INFO',1,stepCt,'ERROR');
	perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	perform cz_end_audit (jobId,'FAIL');
	return 161;
    end if;
    
    begin
	insert into deapp.de_rnaseq_annotation 
		    (gpl_id
		    ,transcript_id 
		    ,gene_symbol
		    ,gene_id
		    ,organism
		    ,probeset_id)
	select g.platform
               ,a.transcript_id
               ,a.gene_symbol
               ,b.bio_marker_id
               ,a.organism
               ,pd.probeset_id
          from tm_lz.lt_rnaseq_annotation a
               ,(select platform from deapp.de_gpl_info where marker_type='RNASEQ') as g
               ,biomart.bio_marker b
               ,probeset_deapp pd
         where b.bio_marker_name=a.gene_symbol
           and a.transcript_id =pd.probeset;
	get diagnostics rowCt := ROW_COUNT;	
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;
    
    stepCt := stepCt + 1;
    performcz_write_audit(jobId,databaseName,procedureName,'Insert new probesets into de_rnaseq_annotation',rowCt,stepCt,'Done');
    
    return 0 ;

end;

$$;

--
-- Name: i2b2_rna_seq_annotation(bigint); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION i2b2_rna_seq_annotation(currentjobid bigint DEFAULT NULL::bigint) RETURNS numeric
    LANGUAGE plpgsql
AS $$
    declare

    gpl_rtn bigint;
    newJobFlag numeric(1);
    databaseName character varying(100);
    procedureName character varying(100);
    jobID bigint;
    errorNumber		character varying;
    errorMessage	character varying;
    rowCt			numeric(18,0);
    stepCt bigint;

begin

    stepCt := 0;

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;
    
    databaseName := 'tm_cz';
    procedureName := 'i2b2_rna_seq_annotation';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(coalesce(jobID::text, '') = '' or jobID < 1) then
	newJobFlag := 1; -- True
	select cz_start_audit (procedureName, databaseName, jobID) into jobId;
    end if;

    stepCt := stepCt + 1;
    perform cz_write_audit(jobId,databaseName,procedureName,'Starting i2b2_rna_seq_annotation',0,stepCt,'Done');
    
    -- insert into deapp.de_rnaseq_annotation
    
    select count(platform) into gpl_rtn
      from deapp.de_gpl_info
     where marker_type='RNASEQ'
       and (platform IS NOT NULL
	    and platform::text <> '');
    if gpl_rtn=0 then
	perform cz_write_audit(jobId,databasename,procedurename,'Platform data missing from DEAPP.DE_GPL_INFO',1,stepCt,'ERROR');
	perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	perform cz_end_audit (jobId,'FAIL');
	return 161;
    end if;
    
    begin
	insert into deapp.de_rnaseq_annotation
		    (transcript_id
		    ,gpl_id
		    ,gene_symbol
		    ,gene_id
		    ,organism)
	select distinct (a.transcript_id)
			,null
			,a.gene_symbol
			,null
			,a.organism
	  from tm_lz.LT_RNASEQ_ANNOTATION a
	 where a.transcript_id not in (select distinct transcript_id from deapp.DE_RNASEQ_ANNOTATION);
	get diagnostics rowCt := ROW_COUNT;	
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;
    
    stepCt := stepCt + 1;
    perform cz_write_audit(jobId,databaseName,procedureName,'Insert data in de_rnaseq_annotation',0,stepCt,'Done');
    
    begin
	update deapp.de_rnaseq_annotation a
	   set gene_id=
	       (select distinct primary_external_id
		  from biomart.bio_marker b
		 where b.bio_marker_name=a.gene_symbol limit 1)
	 where a.GENE_ID is null;
	get diagnostics rowCt := ROW_COUNT;	
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;
    
    stepCt := stepCt + 1;
    perform cz_write_audit(jobId,databaseName,procedureName,'End i2b2_rna_seq_annotation',0,stepCt,'Done');
    
    ---Cleanup OVERALL JOB if this proc is being run standalone
    if newJobFlag = 1 then
	perform cz_end_audit (jobID, 'SUCCESS');
    end if;

    return 0;

end;

$$;

