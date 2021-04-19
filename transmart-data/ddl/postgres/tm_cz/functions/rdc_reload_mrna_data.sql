--
-- Name: rdc_reload_mrna_data(text, text, text, bigint, bigint); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.rdc_reload_mrna_data(trial_id text, data_type text DEFAULT 'R'::text, source_cd text DEFAULT 'STD'::text, log_base numeric DEFAULT 2, currentjobid bigint DEFAULT NULL::bigint) RETURNS void
    LANGUAGE plpgsql
AS $$
    declare


    --	JEA@20111202	One-off to re-zscore gene expression data for a study

    TrialID		varchar(100);
    RootNode		varchar(2000);
    root_level		integer;
    topNode		varchar(2000);
    topLevel		integer;
    tPath		varchar(2000);
    study_name		varchar(100);
    sourceCd		varchar(50);

    dataType		varchar(10);
    sqlText		varchar(1000);
    tText		varchar(1000);
    gplTitle		varchar(1000);
    pExists		bigint;
    partTbl   		bigint;
    partExists 		bigint;
    sampleCt		bigint;
    idxExists 		bigint;
    logBase		numeric;
    pCount		integer;
    sCount		integer;
    tablespaceName	varchar(200);

    --Audit variables
    newJobFlag numeric(1);
    databaseName varchar(100);
    procedureName varchar(100);
    jobID integer;
    stepCt integer;
    rowCt integer;

begin
    TrialID := upper(trial_id);
    --	topNode := REGEXP_REPLACE('\' || top_node || '\','(\\){2,}', '\');
    --	select length(topNode)-length(replace(topNode,'\','')) into topLevel from dual;

    if coalesce(data_type::text, '') = '' then
	dataType := 'R';
    else
	if data_type in ('R','T','L') then
	    dataType := data_type;
	else
	    dataType := 'R';
	end if;
    end if;

    logBase := log_base;
    sourceCd := upper(coalesce(source_cd,'STD'));

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'rdc_reload_mrna_data';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(coalesce(jobID::text, '') = '' or jobID < 1) then
	newJobFlag := 1; -- True
	perform tm_cz.cz_start_audit (procedureName, databaseName, jobID);
    end if;

    stepCt := 0;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Starting i2b2_process_mrna_data',0,stepCt,'Done');

    --	truncate tmp tables

    execute('truncate table tm_wz.wt_subject_microarray_logs');
    execute('truncate table tm_wz.wt_subject_microarray_calcs');
    execute('truncate table tm_wz.wt_subject_microarray_med');

    select count(*)
      into idxExists
      from pg_indexes
     where tablename = 'WT_SUBJECT_MICROARRAY_LOGS'
       and indexname = 'WT_SUBJECT_MRNA_LOGS_I1'
       and owner = 'TM_WZ';

    if idxExists = 1 then
	execute('drop index tm_wz.wt_subject_mrna_logs_i1');
    end if;

    select count(*)
      into idxExists
      from pg_indexes
     where tablename = 'WT_SUBJECT_MICROARRAY_CALCS'
       and indexname = 'WT_SUBJECT_MRNA_CALCS_I1'
       and owner = 'TM_WZ';

    if idxExists = 1 then
	execute('drop index tm_wz.wt_subject_mrna_calcs_i1');
    end if;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Truncate work tables in TM_WZ',0,stepCt,'Done');

    insert into tm_wz.wt_subject_microarray_logs
		(probeset_id
		,intensity_value
		,assay_id
		,log_intensity
		,patient_id
		,sample_id
		,subject_id
		)
    select distinct gs.probeset_id
		    ,avg(md.intensity_value)
		    ,sd.assay_id
		    ,avg(md.intensity_value)
		    ,sd.patient_id
		    ,sd.sample_id
		    ,sd.subject_id
      from deapp.de_subject_sample_mapping sd
	   ,tm_lz.lz_src_mrna_data md
	   ,deapp.de_mrna_annotation gs
     where sd.sample_cd = md.expr_id
       and sd.platform = 'MRNA_AFFYMETRIX'
       and sd.trial_name = TrialId
       and md.trial_name = TrialId
       and sd.gpl_id = gs.gpl_id
       and md.probeset = gs.probe_id
     group by gs.probeset_id
	      ,sd.assay_id
	      ,sd.patient_id
	      ,sd.sample_id
	      ,sd.subject_id;

    stepCt := stepCt + 1; get diagnostics rowCt := ROW_COUNT;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Loaded data for trial in TM_WZ wt_subject_microarray_logs',rowCt,stepCt,'Done');

    commit;

    execute('create index tm_wz.wt_subject_mrna_logs_i1 on tm_wz.wt_subject_microarray_logs (trial_name, probeset_id) nologging  tablespace "INDX"');
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create index on TM_WZ wt_subject_microarray_logs',0,stepCt,'Done');

    --	calculate mean_intensity, median_intensity, and stddev_intensity per experiment, probe

    insert into tm_wz.wt_subject_microarray_calcs
		(trial_name
		,probeset_id
		,mean_intensity
		,median_intensity
		,stddev_intensity
		)
    select d.trial_name
	   ,d.probeset_id
	   ,avg(log_intensity)
	   ,median(log_intensity)
	   ,stddev(log_intensity)
      from tm_wz.wt_subject_microarray_logs d
     group by d.trial_name
	      ,d.probeset_id;
    stepCt := stepCt + 1; get diagnostics rowCt := ROW_COUNT;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Calculate intensities for trial in TM_WZ wt_subject_microarray_calcs',rowCt,stepCt,'Done');

    commit;

    execute('create index tm_wz.wt_subject_mrna_calcs_i1 on tm_wz.wt_subject_microarray_calcs (trial_name, probeset_id) nologging tablespace "INDX"');
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create index on TM_WZ wt_subject_microarray_calcs',0,stepCt,'Done');

    -- calculate zscore

    insert into tm_wz.wt_subject_microarray_med
		(probeset_id
		,intensity_value
		,log_intensity
		,assay_id
		,mean_intensity
		,stddev_intensity
		,median_intensity
		,zscore
		,patient_id
		,sample_id
		,subject_id)
    select d.probeset_id
	   ,d.intensity_value
	   ,d.log_intensity
	   ,d.assay_id
	   ,c.mean_intensity
	   ,c.stddev_intensity
	   ,c.median_intensity
	   ,CASE WHEN stddev_intensity=0 THEN 0 ELSE (log_intensity - median_intensity ) / stddev_intensity END
	   ,d.patient_id
	   ,d.sample_id
	   ,d.subject_id
      from tm_wz.wt_subject_microarray_logs d
	   ,tm_wz.wt_subject_microarray_calcs c
     where d.probeset_id = c.probeset_id;
    stepCt := stepCt + 1; get diagnostics rowCt := ROW_COUNT;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Calculate Z-Score for trial in TM_WZ wt_subject_microarray_med',rowCt,stepCt,'Done');

    commit;

    select count(*)
      into idxExists
      from pg_indexes
     where tablename = 'de_subject_microarray_data'
       and indexname = 'mrna_indx1'
       and owner = 'deapp';

    if idxExists = 0 then
	execute('create index deapp.mrna_idx1 on deapp.de_subject_microarray_data (trial_name) nologging tablespace "INDX"');
    end if;

    delete from deapp.de_subject_microarray_data
     where trial_name = TrialId;

    execute('drop index deapp.mrna_idx1');

    insert into deapp.de_subject_microarray_data
		(trial_name
		,assay_id
		,probeset_id
		,raw_intensity
		,log_intensity
		,zscore
		,patient_id
		,sample_id
		,subject_id
		)
    select TrialId
	   ,m.assay_id
	   ,m.probeset_id
	   ,case when dataType = 'R' then m.intensity_value
	    when dataType = 'L' then case when logBase = -1 then null else power(logBase, m.log_intensity) end
	    else null end
	   ,m.log_intensity
	   ,round(CASE WHEN m.zscore < -2.5 THEN -2.5 WHEN m.zscore >  2.5 THEN  2.5 ELSE round(m.zscore,5) END,5)
	   ,m.patient_id
	   ,m.sample_id
	   ,m.subject_id
      from tm_wz.wt_subject_microarray_med m;
    stepCt := stepCt + 1; get diagnostics rowCt := ROW_COUNT;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert data for trial in DEAPP de_subject_microarray_data',rowCt,stepCt,'Done');

    commit;

    --	cleanup tmp_ files

    execute('truncate table tm_wz.wt_subject_microarray_logs');
    execute('truncate table tm_wz.wt_subject_microarray_calcs');
    execute('truncate table tm_wz.wt_subject_microarray_med');

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Truncate work tables in TM_WZ',0,stepCt,'Done');

    ---Cleanup OVERALL JOB if this proc is being run standalone

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'End i2b2_process_mrna_data',0,stepCt,'Done');

    if newJobFlag = 1 then
	perform tm_cz.cz_end_audit (jobID, 'SUCCESS');
    end if;

    --select 0 into rtn_code from dual;

exception
    when others then
    --Handle errors.
	perform tm_cz.cz_error_handler(jobId, procedureName, SQLSTATE, SQLERRM);
    --End Proc
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
    --select 16 into rtn_code from dual;
end;

$$;

