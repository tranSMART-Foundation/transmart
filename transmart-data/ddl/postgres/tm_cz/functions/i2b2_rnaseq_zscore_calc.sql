--
-- Name: i2b2_rnaseq_zscore_calc(character varying, character varying, character varying, numeric, character varying, numeric, character varying, bigint, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_rnaseq_zscore_calc(trial_id character varying, partition_name character varying, partition_indx character varying, partitionid numeric, run_type character varying DEFAULT 'L'::character varying, currentjobid numeric DEFAULT 0, data_type character varying DEFAULT 'R'::character varying, log_base numeric DEFAULT 2, source_cd character varying DEFAULT NULL::character varying) RETURNS numeric
    LANGUAGE plpgsql
    AS $$
DECLARE

    /*************************************************************************
     * This stored procedure is used to calculate z-scores for RNA sequencing data load
     * Date:10/23/2013
     ******************************************************************/

    TrialID varchar(50);
    sourceCD	varchar(50);
    sqlText varchar(2000);
    runType varchar(10);
    dataType varchar(10);
    stgTrial varchar(50);
    idxExists bigint;
    pExists	bigint;
    nbrRecs bigint;
    logBase numeric;
    partitionName varchar(200);
    partitionindx varchar(200);

    --Audit variables
    newJobFlag integer;
    databaseName varchar(100);
    procedureName varchar(100);
    jobID bigint;
    stepCt bigint;
    rowCt			bigint;
    errorNumber		character varying;
    errorMessage	character varying;

    cleanTablesValue	character varying(255);

begin

    select paramvalue
      into cleanTablesValue
      from tm_cz.etl_settings
     where paramname in ('cleantables','CLEANTABLES');

    TrialId := trial_id;
    runType := run_type;
    dataType := data_type;
    logBase := log_base;
    sourceCd := source_cd;

    partitionindx := partition_indx;
    partitionName := partition_name;

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_rnaseq_zscore_calc';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(coalesce(jobID::text, '') = '' or jobID < 1) then
	newJobFlag := 1; -- True
	perform tm_cz.cz_start_audit (procedureName, databaseName, jobID);
    end if;

    stepCt := 0;

    select count(*) into pExists
      from information_schema.tables
     where table_name = partitionindx;

    --	calculate zscore and insert to partition

    execute ('drop index if exists tm_wz.wt_subject_rnaseq_logs_i1');
    execute ('drop index if exists tm_wz.wt_subject_rnaseq_calcs_i1');
    execute ('truncate table tm_wz.wt_subject_rnaseq_logs');
    execute ('truncate table tm_wz.wt_subject_rnaseq_calcs');
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Drop indexes and truncate zscore work tables',1,stepCt,'Done');

    begin
	insert into tm_wz.wt_subject_rnaseq_logs (
	    region_id
	    ,readcount
	    ,assay_id
	    ,raw_readcount
	    ,log_readcount
	    ,patient_id
	    ,trial_name
	)
	select   region_id
		 ,readcount
		 ,assay_id
		 ,case when dataType = 'R'
		     then normalized_readcount
		  else case when logBase = -1 then 0 else power(logBase,normalized_readcount) end
		  end
		 ,case when dataType = 'L' then normalized_readcount else log(logBase,normalized_readcount::numeric) end
		 ,patient_id
		 ,trial_name
	  from tm_wz.wt_subject_rnaseq_region;
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Loaded data for trial in TM_WZ wt_subject_rnaseq_logs',rowCt,stepCt,'Done');

    execute ('create index wt_subject_rnaseq_logs_i1 on tm_wz.wt_subject_rnaseq_logs (region_id) tablespace "indx"');
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create index on TM_WZ wt_subject_rnaseq_logs',0,stepCt,'Done');

    --	calculate mean_intensity, median_intensity, and stddev_intensity per region

    begin
	insert into tm_wz.wt_subject_rnaseq_calcs (
	    region_id
	    ,mean_readcount
	    ,median_readcount
	    ,stddev_readcount)
	select   d.region_id
		 ,avg(log_readcount)
		 ,median(log_readcount)
		 ,stddev(log_readcount)
	  from tm_wz.wt_subject_rnaseq_logs d
	 where log_readcount is not null -- remove null values because median function cannot handle (returns null)
	 group by d.region_id;
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Calculate mean, median, stddev readcounts for trial in TM_WZ wt_subject_rnaseq_calcs',rowCt,stepCt,'Done');

    execute ('create index wt_subject_rnaseq_calcs_i1 on tm_wz.wt_subject_rnaseq_calcs (region_id) tablespace "indx"');
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create index on TM_WZ wt_subject_rnaseq_calcs',0,stepCt,'Done');

    -- calculate zscore and insert into partition

    sqlText := 'insert into ' || partitionName || ' (partition_id, region_id, assay_id, patient_id, trial_name, readcount, normalized_readcount, log_normalized_readcount, zscore) ' ||
	'select ' || partitionId::text || ', d.region_id, d.assay_id, d.patient_id, d.trial_name, d.readcount, round(d.raw_readcount::numeric,6) , round(d.log_readcount::numeric,6), ' ||
	'case when c.stddev_readcount = 0 then 0 else ' ||
	'case when (d.log_readcount - c.median_readcount ) / c.stddev_readcount < -2.5 then -2.5 ' ||
	'when (d.log_readcount - c.median_readcount ) / c.stddev_readcount > 2.5 then 2.5 else ' ||
	'round( ((d.log_readcount - c.median_readcount) / c.stddev_readcount)::numeric,6) end end ' ||
	'from tm_wz.wt_subject_rnaseq_logs d ' ||
	',tm_wz.wt_subject_rnaseq_calcs c ' ||
	'where d.region_id = c.region_id';
    raise notice 'sqlText= %', sqlText;
    execute sqlText;
    get diagnostics rowCt := ROW_COUNT;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Calculate Z-Score and insert into ' || partitionName,rowCt,stepCt,'Done');

    --	create indexes on partition

    sqlText := ' create index ' || partitionIndx || '_idx1 on ' || partitionName || ' using btree (partition_id) tablespace indx';
    raise notice 'sqlText= %', sqlText;
    execute sqlText;
    sqlText := ' create index ' || partitionIndx || '_idx2 on ' || partitionName || ' using btree (assay_id) tablespace indx';
    raise notice 'sqlText= %', sqlText;
    execute sqlText;
    sqlText := ' create index ' || partitionIndx || '_idx3 on ' || partitionName || ' using btree (region_id) tablespace indx';
    raise notice 'sqlText= %', sqlText;
    execute sqlText;
    sqlText := ' create index ' || partitionIndx || '_idx4 on ' || partitionName || ' using btree (assay_id, region_id) tablespace indx';
    raise notice 'sqlText= %', sqlText;
    execute sqlText;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Created indexes on '||partitionName,4,stepCt,'Done');

    ---Cleanup OVERALL JOB if this proc is being run standalone
    if newJobFlag = 1 then
	perform tm_cz.cz_end_audit (jobID, 'SUCCESS');
    end if;

    return 1;
end;

$$;

