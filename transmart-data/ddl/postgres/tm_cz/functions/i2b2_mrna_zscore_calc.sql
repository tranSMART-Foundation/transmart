--
-- Name: i2b2_mrna_zscore_calc(character varying, character varying, character varying, numeric, character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_mrna_zscore_calc(trial_id character varying, partition_name character varying, partition_indx character varying, partitionid numeric, run_type character varying DEFAULT 'L'::character varying, currentjobid numeric DEFAULT 0, data_type character varying DEFAULT 'R'::character varying, log_base numeric DEFAULT 2, source_cd character varying DEFAULT NULL::character varying) RETURNS numeric
    LANGUAGE plpgsql
AS $$
    declare

    /*************************************************************************
     * Copyright 2008-2012 Janssen Research and Development, LLC.
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     * http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     *************************************************************************/

    TrialID varchar(50);
    sourceCD	varchar(50);
    sqlText varchar(2000);
    runType varchar(10);
    dataType varchar(10);
    stgTrial varchar(50);
    idxExists integer;
    pExists	integer;
    nbrRecs integer;
    logBase numeric;
    partitionName varchar(200);
    partitionindx varchar(200);

    --Audit variables
    newJobFlag numeric(1);
    databaseName varchar(100);
    procedureName varchar(100);
    jobID numeric;
    stepCt integer;
    rowCt integer;
    errorNumber		character varying;
    errorMessage	character varying;
    errorStack		character varying;

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
    procedureName := 'i2b2_mrna_zscore_calc';

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

    if pExists = 0 then
	sqlText := 'create table ' || partitionName || ' ( constraint mrna_' || partitionId::text || '_check check ( partition_id = ' || partitionId::text ||
	    ')) inherits (deapp.de_subject_microarray_data)';
	raise notice 'sqlText= %', sqlText;
	execute sqlText;
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create partition ' || partitionName,1,stepCt,'Done');
    else
        -- Keep this statement for backward compatibility
	sqlText := 'drop index if exists ' || partitionIndx || '_idx1';
--	raise notice 'sqlText= %', sqlText;
	execute sqlText;
	sqlText := 'drop index if exists ' || partitionIndx || '_idx2';
--	raise notice 'sqlText= %', sqlText;
	execute sqlText;
	sqlText := 'drop index if exists ' || partitionIndx || '_idx3';
--	raise notice 'sqlText= %', sqlText;
	execute sqlText;
	sqlText := 'drop index if exists ' || partitionIndx || '_idx4';
--	raise notice 'sqlText= %', sqlText;
	execute sqlText;
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Drop indexes on ' || partitionName,1,stepCt,'Done');
	sqlText := 'truncate table ' || partitionName;
--	raise notice 'sqlText= %', sqlText;
	execute sqlText;
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Truncate ' || partitionName,1,stepCt,'Done');
    end if;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Starting mrna zscore calc for ' || TrialId || ' RunType: ' || runType || ' dataType: ' || dataType,0,stepCt,'Done');

    if runType != 'L' then
	stepCt := stepCt + 1;
	get diagnostics rowCt := ROW_COUNT;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Invalid runType passed - procedure exiting',
				     rowCt,stepCt,'Done');
	--Handle errors.
    	perform tm_cz.cz_error_handler(jobId, procedureName, errorNumber, errorMessage);
    	--End Proc
    	perform tm_cz.cz_end_audit (jobID, 'FAIL');
        return -16;
    end if;

    --	For Load, make sure that the TrialId passed as parameter is the same as the trial in stg_subject_mrna_data
    --	If not, raise exception

    if runType = 'L' then
	select distinct trial_name into stgTrial
	from tm_wz.wt_subject_mrna_probeset;

	if stgTrial != TrialId then
	    stepCt := stepCt + 1;
	    get diagnostics rowCt := ROW_COUNT;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'TrialId not the same as trial in wt_subject_mrna_probeset - procedure exiting',
					 rowCt,stepCt,'Done');
	    --Handle errors.
    	    perform tm_cz.cz_error_handler(jobId, procedureName, errorNumber, errorMessage);
    	    --End Proc
    	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
            return -16;
	end if;
    end if;

    --	truncate tmp tables

    execute('truncate table tm_wz.wt_subject_microarray_logs');
    execute('truncate table tm_wz.wt_subject_microarray_calcs');
    execute('truncate table tm_wz.wt_subject_microarray_med');

    execute('drop index if exists tm_wz.wt_subject_mrna_logs_i1');
    execute('drop index if exists tm_wz.wt_subject_mrna_calcs_i1');

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Truncate work tables in TM_WZ',0,stepCt,'Done');

    --	if dataType = L, use intensity_value as log_intensity
    --	if dataType = R, always use intensity_value

    begin
	if dataType = 'L' then
	    insert into tm_wz.wt_subject_microarray_logs (
		probeset_id
		,intensity_value
		,assay_id
		,log_intensity
		,patient_id
	    )
	    select probeset_id
	    ,intensity_value
	    ,assay_id
	    ,intensity_value
	    ,patient_id
	from tm_wz.wt_subject_mrna_probeset
	    where trial_name = TrialId;
	else
	    insert into tm_wz.wt_subject_microarray_logs (
		probeset_id
		,intensity_value
		,assay_id
		,log_intensity
		,patient_id
	    )
	    select probeset_id
		   ,intensity_value
		   ,assay_id
		   ,round((CASE WHEN intensity_value <= 0 THEN 0
			   ELSE ln(intensity_value)/ln(logBase::double precision) END)::numeric,5)
		   ,patient_id
	      from tm_wz.wt_subject_mrna_probeset
	     where trial_name = TrialId;
	end if;
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
    get diagnostics rowCt := ROW_COUNT;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Loaded data for trial in TM_WZ wt_subject_microarray_logs',rowCt,stepCt,'Done');

    -- trial_name no longer in this table
    --execute('create index wt_subject_mrna_logs_i1 on tm_wz.wt_subject_microarray_logs (trial_name, probeset_id) tablespace "indx"');
    --stepCt := stepCt + 1;
    --perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create index on TM_WZ wt_subject_microarray_logs',0,stepCt,'Done');

    --	calculate mean_intensity, median_intensity, and stddev_intensity per experiment, probe

    begin
	insert into tm_wz.wt_subject_microarray_calcs (
	    probeset_id
	    ,mean_intensity
	    ,median_intensity
	    ,stddev_intensity
	    ,trial_name
	)
	select d.probeset_id
	       ,avg(log_intensity)
	       ,median(log_intensity)
	       ,stddev(log_intensity)
	       ,TrialID
	  from tm_wz.wt_subject_microarray_logs d
	 group by d.probeset_id;
	stepCt := stepCt + 1;
	get diagnostics rowCt := ROW_COUNT;
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	    get diagnostics errorStack := PG_CONTEXT;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage, errorStack);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;

    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Calculate intensities for trial in TM_WZ wt_subject_microarray_calcs',rowCt,stepCt,'Done');

    begin
	EXECUTE('create index wt_subject_mrna_calcs_i1 on tm_wz.wt_subject_microarray_calcs (trial_name, probeset_id) tablespace "indx"');
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create index on TM_WZ wt_subject_microarray_calcs',0,stepCt,'Done');

    -- calculate zscore

    begin
	insert into tm_wz.wt_subject_microarray_med (
	    probeset_id
	    ,intensity_value
	    ,log_intensity
	    ,assay_id
	    ,mean_intensity
	    ,stddev_intensity
	    ,median_intensity
	    ,zscore
	    ,patient_id
	)
	select d.probeset_id
	       ,d.intensity_value
	       ,d.log_intensity
	       ,d.assay_id
	       ,c.mean_intensity
	       ,c.stddev_intensity
	       ,c.median_intensity
	       ,CASE WHEN stddev_intensity=0 THEN 0 ELSE (log_intensity - median_intensity ) / stddev_intensity END
	       ,d.patient_id
	  from tm_wz.wt_subject_microarray_logs d
	       ,tm_wz.wt_subject_microarray_calcs c
	 where d.probeset_id = c.probeset_id;
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Calculate Z-Score for trial in TM_WZ wt_subject_microarray_med',rowCt,stepCt,'Done');

    begin
	sqlText := 'insert into ' || partitionName || ' (partition_id, trial_name, probeset_id, assay_id, patient_id' ||
	    ', raw_intensity' ||
	    ', log_intensity' ||
	    ', zscore) ' ||
	    'select ' || partitionId::text || ', d.trial_name, d.probeset_id, d.assay_id, d.patient_id' ||
	    ', round(case when ''' || dataType || '''= ''R'' then d.intensity_value::numeric' ||
	    ' when '''||  dataType || '''= ''L'' then case when ' || logBase || '= -1 then null else power(' || logBase || ', d.log_intensity::numeric) end ' ||
	    ' else null end, 4) as raw_intensity ' ||
	    ', d.log_intensity, ' ||
	    'case when c.stddev_intensity = 0 then 0 else ' ||
	    'case when (d.log_intensity - c.median_intensity ) / c.stddev_intensity < -2.5 then -2.5 ' ||
	    'when (d.log_intensity - c.median_intensity ) / c.stddev_intensity > 2.5 then 2.5 else ' ||
	    '(d.log_intensity - c.median_intensity ) / c.stddev_intensity end end ' ||
	    'from tm_wz.wt_subject_microarray_logs d ' ||
	    ',tm_wz.wt_subject_microarray_calcs c ' ||
	    'where d.probeset_id = c.probeset_id';

--	raise notice 'sqlText= %', sqlText;
	execute sqlText;
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert data for trial in DEAPP de_subject_microarray_data',rowCt,stepCt,'Done');


    --	cleanup tmp_ files

    if (coalesce(cleanTablesValue,'yes')) then
	execute('truncate table tm_wz.wt_subject_microarray_logs');
	execute('truncate table tm_wz.wt_subject_microarray_calcs');
	execute('truncate table tm_wz.wt_subject_microarray_med');

	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Truncate work tables in TM_WZ',0,stepCt,'Done');
    end if;

    --	create indexes on partition
    sqlText := ' create index ' || partitionIndx || '_idx2 on ' || partitionName || ' using btree (assay_id) tablespace indx';
    raise notice 'sqlText= %', sqlText;
    execute sqlText;
    sqlText := ' create index ' || partitionIndx || '_idx3 on ' || partitionName || ' using btree (probeset_id) tablespace indx';
    raise notice 'sqlText= %', sqlText;
    execute sqlText;
    sqlText := ' create index ' || partitionIndx || '_idx4 on ' || partitionName || ' using btree (assay_id, probeset_id) tablespace indx';
    raise notice 'sqlText= %', sqlText;
    execute sqlText;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Created indexes on '||partitionName,3,stepCt,'Done');

    ---Cleanup OVERALL JOB if this proc is being run standalone
    if newJobFlag = 1 then
	perform tm_cz.cz_end_audit (jobID, 'SUCCESS');
    end if;

    return 1;

exception
    when others then
	errorNumber := SQLSTATE;
	errorMessage := SQLERRM;
    --handle errors.
	perform tm_cz.cz_error_handler(jobId, procedureName, errorNumber, errorMessage);
    --End Proc
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
	return -166;

end;

$$;

