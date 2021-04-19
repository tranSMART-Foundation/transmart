--
-- Name: i2b2_load_chrom_region(character varying, character varying, character varying, character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_load_chrom_region(platform_title character varying DEFAULT ''::character varying,
                                       data_type      character varying DEFAULT 'ACGH'::character varying,  -- valid values are ACGH and RNASEQ
                                       genome_release character varying DEFAULT ''::character varying,
                                       gene_annotation_id character varying DEFAULT ''::character varying,
                                       currentjobid numeric DEFAULT (-1))
    RETURNS numeric
    LANGUAGE plpgsql
AS $$

    declare

    --Audit variables
    newJobFlag		integer;
    databaseName 		VARCHAR(100);
    procedureName 		VARCHAR(100);
    jobID 			numeric(18,0);
    stepCt 			numeric(18,0);
    rowCt			numeric(18,0);
    errorNumber		character varying;
    errorMessage	character varying;

    gplId			character varying;
    marker_type		character varying;
    organismId		character varying;
    sqlText			varchar(1000);

begin
    stepCt := 0;

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_load_chrom_region';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it

    if(jobID IS NULL or jobID < 1) then
	newJobFlag := 1; -- True
	select tm_cz.cz_start_audit (procedureName, databaseName) into jobID;
    end if;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Starting i2b2_load_chrom_region',0,stepCt,'Done');

    -- The data should already be in the landing zone (tm_lz.lt_chromosomal_region)


    -- We now do some basic check's:
    -- + is chromosomal_region already in deapp.de_chromosomal_region (gpl_id/region_name)
    --   if true then remove these lines?
    -- + ...


    -- insert region definitions into deapp-schema

    -- First remove previous definitions for gpl_id
    select distinct gpl_id INTO gplId FROM tm_lz.lt_chromosomal_region;

    begin
	delete from deapp.de_chromosomal_region
	 where gpl_id = gplId;
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete existing data from deapp.de_chromosomal_region for plaform: ' || gplId,rowCt,stepCt,'Done');

    begin
	delete from deapp.de_gpl_info
	 where platform = gplId;
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete existing data from deapp.de_gpl_info for platform: ' || gplID,rowCt,stepCt,'Done');


    -- Insert platform into deapp.de_gpl_info

    -- Derive marker_type from data_type argument (defaults to CHROMOSOME_REGION_ACGH)
    if (upper(data_type) = 'RNASEQ')
    then
	marker_type := 'RNASEQ_RCNT';
    else
	marker_type := 'Chromosomal';
    end if;

    if (length(platform_title) = 0)
    then
	platform_title = gplId;
	end if;

    select distinct organism INTO organismId FROM tm_lz.lt_chromosomal_region;
    begin
	insert into deapp.de_gpl_info
		    (platform
		    ,title
		    ,organism
		    ,annotation_date
		    ,marker_type
		    ,genome_build
		    ,gene_annotation_id)
	values (gplId
		,platform_title
		,organismId
		,current_timestamp
		,marker_type
		,genome_release
		,gene_annotation_id);
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Load platform info into deapp.de_gpl_info for platform: ' || gplID,rowCt,stepCt,'Done');


    -- Next insert the new definitions
    begin
	insert into deapp.de_chromosomal_region
  		    (gpl_id
		    ,region_name
		    ,chromosome
		    ,start_bp
		    ,end_bp
		    ,num_probes
		    ,cytoband
		    ,gene_symbol
		    ,gene_id
		    ,organism)
	select lz.gpl_id
	       ,lz.region_name
	       ,lz.chromosome
	       ,lz.start_bp
	       ,lz.end_bp
	       ,lz.num_probes
	       ,lz.cytoband
	       ,lz.gene_symbol
	       ,lz.gene_id
	       ,lz.organism
	  from tm_lz.lt_chromosomal_region lz;
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Load chromosomal region data into deapp.de_chromosomal_region for platform: ' || gplId,rowCt,stepCt,'Done');


    --	update gene_id if null

    begin
	with upd as (select b.bio_marker_name as gene_symbol
			    ,b.organism
			    ,min(b.primary_external_id::numeric) as gene_id
		       from biomart.bio_marker b
		      where upper(b.bio_marker_type) = 'GENE'
		      group by b.bio_marker_name, b.organism)
		update deapp.de_chromosomal_region a
		set gene_id=upd.gene_id
		from upd
		where a.gpl_id = gplId
		and a.gene_id is null
		and a.gene_symbol is not null
		and a.gene_symbol = upd.gene_symbol
		and upper(a.organism) = upper(upd.organism)
		and exists
		(select 1 from biomart.bio_marker x
		  where a.gene_symbol = x.bio_marker_name
		    and upper(x.organism) = upper(a.organism)
		    and upper(x.bio_marker_type) = 'GENE');
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Updated missing gene_id in de_chromosomal_region',rowCt,stepCt,'Done');


    --	update gene_symbol if null

    begin
	with upd as (select b.primary_external_id::numeric as gene_id
			    ,b.organism
			    ,min(b.bio_marker_name) as gene_symbol
		       from biomart.bio_marker b
		      where upper(b.bio_marker_type) = 'GENE'
		      group by b.primary_external_id, b.organism)
		update deapp.de_chromosomal_region a
		set gene_symbol=upd.gene_symbol
		from upd
		where a.gpl_id = gplId
		and a.gene_symbol is null
		and a.gene_id is not null
		and a.gene_id = upd.gene_id
		and a.organism = upd.organism
		and exists
		(select 1 from biomart.bio_marker x
		  where a.gene_id::varchar = x.primary_external_id
		    and upper(x.organism) = upper(a.organism)
		    and upper(x.bio_marker_type) = 'GENE');
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Updated missing gene_symbol in de_chromosomal_region',rowCt,stepCt,'Done');


    -- wrapping up
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'End i2b2_load_chrom_region',0,stepCt,'Done');

    ---Cleanup OVERALL JOB if this proc is being run standalone
    if newJobFlag = 1 THEN
	perform tm_cz.cz_end_audit (jobID, 'SUCCESS');
    end if;

    return 1;

end;

$$;

