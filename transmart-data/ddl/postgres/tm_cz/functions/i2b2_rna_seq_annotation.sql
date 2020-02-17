--
-- Name: i2b2_rna_seq_annotation(bigint); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_rna_seq_annotation( platformId character varying, currentjobid bigint DEFAULT NULL::bigint) RETURNS numeric
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
    rowCt		numeric(18,0);
    tText		varchar(1000);
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
	select tm_cz.cz_start_audit (procedureName, databaseName, jobID) into jobId;
    end if;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Starting i2b2_rna_seq_annotation',0,stepCt,'Done');
    
    -- insert into deapp.de_rnaseq_annotation
    
    select count(platform) into gpl_rtn
      from deapp.de_gpl_info
     where marker_type='RNASEQCOG'
       and platform = platformId;
    if gpl_rtn=0 then
        tText := 'Platform data missing from DEAPP.DE_GPL_INFO for ' || platformId;
       	perform tm_cz.cz_write_audit(jobId,databasename,procedurename,tText,1,stepCt,'ERROR');
       	perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
       	perform tm_cz.cz_end_audit (jobId,'FAIL');
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
			,platformId
			,a.gene_symbol
			,case when a.gene_id is null then null when a.gene_id='' then null else a.gene_id::numeric end as gene_id
			,a.organism
	  from tm_lz.lt_rnaseq_annotation a
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert data in de_rnaseq_annotation',rowCt,stepCt,'Done');
    
    begin
	update deapp.de_rnaseq_annotation a
	   set gene_id=
	       (select min(b.primary_external_id::numeric)
		  from biomart.bio_marker b
		 where b.bio_marker_name = a.gene_symbol
		   and b.organism = a.organism
		 limit 1)
	 where a.gene_id is null
	 and a.gene_symbol is not null;
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update missing gene_id values in i2b2_rna_seq_annotation',rowCt,stepCt,'Done');
    
    ---Cleanup OVERALL JOB if this proc is being run standalone
    if newJobFlag = 1 then
	perform tm_cz.cz_end_audit (jobID, 'SUCCESS');
    end if;

    return 0;

end;

$$;

