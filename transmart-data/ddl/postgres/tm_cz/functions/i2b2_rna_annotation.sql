--
-- Name: i2b2_rna_annotation(character varying,bigint); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_rna_annotation( platformId character varying, currentjobid bigint DEFAULT NULL::bigint) RETURNS numeric
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
    procedureName := 'i2b2_rna_annotation';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(coalesce(jobID::text, '') = '' or jobID < 1) then
	newJobFlag := 1; -- True
	select tm_cz.cz_start_audit (procedureName, databaseName, jobID) into jobId;
    end if;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Starting i2b2_rna_annotation',0,stepCt,'Done');

    -- check platform has been added to deapp.de_gpl_info

    select count(platform) into gpl_rtn
      from deapp.de_gpl_info
     where marker_type='RNASEQCOG'
       and platform = platformId;

    if gpl_rtn=0 then
        tText := 'Platform data missing from deapp.de_gpl_info for ' || platformId;
       	perform tm_cz.cz_write_audit(jobId,databasename,procedurename,tText,1,stepCt,'ERROR');
       	perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
       	perform tm_cz.cz_end_audit (jobId,'FAIL');
       	return 161;
    end if;

    -- insert into deapp.de_rna_annotation

    begin
	insert into deapp.de_rna_annotation (
	    transcript_id
	    ,gpl_id
	    ,gene_symbol
	    ,gene_id
	    ,organism)
	select
	    distinct (a.transcript_id)
	    ,platformId
	    ,case when a.gene_symbol is null then null when a.gene_symbol='' then null else a.gene_symbol end as gene_symbol
	    ,case when a.gene_id is null then null when a.gene_id='' then null else a.gene_id end as gene_id
	    ,a.organism
	  from tm_lz.lt_rna_annotation a
	 where a.transcript_id not in (
	     select distinct b.transcript_id
	       from deapp.de_rna_annotation b
	      where b.gpl_id = platformId);
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert data in de_rna_annotation',rowCt,stepCt,'Done');

    select count(transcript_id) into gpl_rtn
      from deapp.de_rna_annotation a
     where a.gpl_id = platformId
       and a.gene_id is null
       and a.gene_symbol is not null;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Count missing gene_id values',gpl_rtn,stepCt,'Done');

    if(gpl_rtn > 0) then
        begin
	    update deapp.de_rna_annotation a
	       set gene_id=
		   (select b.primary_external_id
		      from biomart.bio_marker b
		     where b.bio_marker_name = a.gene_symbol
		       and b.bio_marker_type = 'GENE'
		       and upper(b.organism) = upper(a.organism)
		     order by b.primary_external_id
		     limit 1)
	     where a.gpl_id = platformId
		   and a.gene_id is null
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
        perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update missing gene_id values in i2b2_rna_annotation',rowCt,stepCt,'Done');
    end if;

    ---Cleanup OVERALL JOB if this proc is being run standalone
    if newJobFlag = 1 then
	perform tm_cz.cz_end_audit (jobID, 'SUCCESS');
    end if;

    return 1;

end;

$$;

