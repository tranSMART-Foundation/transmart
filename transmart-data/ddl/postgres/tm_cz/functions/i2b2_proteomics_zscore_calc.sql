--
-- Name: i2b2_proteomics_zscore_calc(character varying, character varying, numeric, character varying, numeric, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_proteomics_zscore_calc(trial_id character varying, run_type character varying DEFAULT 'L'::character varying, currentjobid numeric DEFAULT NULL::numeric, data_type character varying DEFAULT 'R'::character varying, log_base numeric DEFAULT 2.0, source_cd character varying DEFAULT NULL::character varying) RETURNS numeric
    LANGUAGE plpgsql SECURITY DEFINER
AS $$

    /*************************************************************************
     * This Stored Procedure is used in ETL load PROTEOMICS data
     *   Date:12/9/2013
     ******************************************************************/

    declare

    TrialID character varying(50);
    sourceCD	character varying(50);
    sqlText character varying(2000);
    runType character varying(10);
    dataType character varying(10);
    stgTrial character varying(50);
    idxExists numeric;
    pExists	numeric;
    nbrRecs numeric;
    logBase numeric;

    --Audit variables
    newJobFlag numeric(1);
    databaseName VARCHAR(100);
    procedureName VARCHAR(100);
    jobID numeric(18,0);
    stepCt numeric(18,0);
    rowCt integer;

begin

    TrialId := trial_id;
    runType := run_type;
    dataType := data_type;
    logBase := log_base;
    sourceCd := source_cd;

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_proteomics_zscore_calc';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(jobID is NULL or jobID < 1) then
	newJobFlag := 1; -- True
	select tm_cz.cz_start_audit (procedureName, databaseName) into jobID;
    end if;

    stepCt := 0;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Starting zscore calc for ' || TrialId || ' RunType: ' || runType || ' dataType: ' || dataType,0,stepCt,'Done');

    if runType != 'L' then
	stepCt := stepCt + 1;
	get diagnostics rowCt := ROW_COUNT;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Invalid runType passed - procedure exiting',rowCt,stepCt,'Done');
	perform tm_cz.cz_error_handler(jobId, procedureName, SQLSTATE, SQLERRM);
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
	return 150;
    end if;

    --	For Load, make sure that the TrialId passed as parameter is the same as the trial in WT_SUBJECT_PROTEOMICS_PROBESET
    --	If not, raise exception

    if runType = 'L' then
	select distinct trial_name into stgTrial
	from tm_wz.wt_subject_proteomics_probeset;

	if stgTrial != TrialId then
	    stepCt := stepCt + 1;
	    get diagnostics rowCt := ROW_COUNT;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'TrialId not the same as trial in WT_SUBJECT_PROTEOMICS_PROBESET - procedure exiting',rowCt,stepCt,'Done');
	    perform tm_cz.cz_error_handler(jobId, procedureName, SQLSTATE, SQLERRM);
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return 161;
	end if;
    end if;

    --	truncate tmp tables

    begin
	execute ('truncate table tm_wz.wt_subject_proteomics_logs');
	execute ('truncate table tm_wz.wt_subject_proteomics_calcs');
	execute ('truncate table tm_wz.wt_subject_proteomics_med');
    exception
	when others then
	    perform tm_cz.cz_error_handler (jobID, procedureName, SQLSTATE, SQLERRM);
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;

    --drop index if exists tm_wz.wt_subject_proteomics_logs_i1;
    --drop index if exists tm_wz.wt_subject_proteomics_calcs_i1;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Truncate work tables in TM_WZ',0,stepCt,'Done');

    --	if dataType = L, use intensity_value as log_intensity
    --	if dataType = R, always use intensity_value

    if dataType = 'L' then
	begin
	    insert into tm_wz.wt_subject_proteomics_logs (
		probeset_id
		,intensity_value
		,assay_id
		,log_intensity
		,patient_id
		--	,sample_cd
		,subject_id)
	    select probeset
		   ,intensity_value ----UAT 154 changes done on 19/03/2014
		   ,assay_id
		   ,round(intensity_value::numeric,4)
		   ,patient_id
		-- ,sample_cd
		   ,subject_id
	      from tm_wz.wt_subject_proteomics_probeset
	     where trial_name = TrialId;
	exception
	    when others then
		perform tm_cz.cz_error_handler (jobID, procedureName, SQLSTATE, SQLERRM);
		perform tm_cz.cz_end_audit (jobID, 'FAIL');
		return -16;
	end;
    else
	begin
            insert into wt_subject_proteomics_logs  (
		probeset_id
		,intensity_value
		,assay_id
		,log_intensity
		,patient_id
		--  ,sample_cd
		,subject_id)
	    select probeset
		   ,intensity_value  ----UAT 154 changes done on 19/03/2014
		   ,assay_id
		   ,round(log(2.0::numeric,intensity_value::numeric  + 0.001),4)  ----UAT 154 changes done on 19/03/2014
		   ,patient_id
		-- ,sample_cd
		   ,subject_id
	      from tm_wz.wt_subject_proteomics_probeset
	     where trial_name = TrialId;
	exception
	    when others then
		perform tm_cz.cz_error_handler (jobID, procedureName, SQLSTATE, SQLERRM);
		perform tm_cz.cz_end_audit (jobID, 'FAIL');
		return -16;
	end;
    end if;

    stepCt := stepCt + 1;
    get diagnostics rowCt := ROW_COUNT;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Loaded data for trial in TM_WZ.wt_subject_proteomics_logs',rowCt,stepCt,'Done');

    --execute ('create index wt_subject_proteomics_logs_I1 on tm_wz.wt_subject_proteomics_logs (trial_name, probeset_id)');
    --stepCt := stepCt + 1;
    --perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create index on TM_WZ WT_SUBJECT_PROTEOMICS_LOGS_I1',0,stepCt,'Done');

    --	calculate mean_intensity, median_intensity, and stddev_intensity per experiment, probe

    begin
	insert into wt_subject_proteomics_calcs (
	    trial_name
	    ,probeset_id
	    ,mean_intensity
	    ,median_intensity
	    ,stddev_intensity)
	select d.trial_name
	       ,d.probeset_id
	       ,avg(log_intensity)
	       ,median(log_intensity)
	       ,stddev(log_intensity)
	  from tm_wz.wt_subject_proteomics_logs d
	 group by d.trial_name
		  ,d.probeset_id;
    exception
	when others then
	    perform tm_cz.cz_error_handler (jobID, procedureName, SQLSTATE, SQLERRM);
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;

    stepCt := stepCt + 1;
    get diagnostics rowCt := ROW_COUNT;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Calculate intensities for trial in TM_WZ WT_SUBJECT_PROTEOMICS_CALCS',rowCt,stepCt,'Done');

    -- execute ('create index tm_wz.wt_subject_proteomics_calcs_i1 on tm_wz.WT_SUBJECT_PROTEOMICS_CALCS (trial_name, probeset_id) nologging tablespace "INDX"');
    -- stepCt := stepCt + 1;
    -- tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create index on TM_WZ WT_SUBJECT_PROTEOMICS_CALCS',0,stepCt,'Done');

    -- calculate zscore

    begin
	insert into wt_subject_proteomics_med  (
	    probeset_id
	    ,intensity_value
	    ,log_intensity
	    ,assay_id
	    ,mean_intensity
	    ,stddev_intensity
	    ,median_intensity
	    ,zscore
	    ,patient_id
	    --	,sample_cd
	    ,subject_id
	)
	select d.probeset_id
	       ,d.intensity_value
	       ,d.log_intensity
	       ,d.assay_id
	       ,c.mean_intensity
	       ,c.stddev_intensity
	       ,c.median_intensity
	       ,(case when stddev_intensity=0 then 0 else (log_intensity - median_intensity ) / stddev_intensity end)
	       ,d.patient_id
	    --	  ,d.sample_cd
	       ,d.subject_id
	  from TM_WZ.WT_SUBJECT_PROTEOMICS_LOGS d
	       ,TM_WZ.WT_SUBJECT_PROTEOMICS_CALCS c
	 where d.probeset_id = c.probeset_id;
    exception
	when others then
	    perform tm_cz.cz_error_handler (jobID, procedureName, SQLSTATE, SQLERRM);
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;

    stepCt := stepCt + 1;
    get diagnostics rowCt := ROW_COUNT;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Calculate Z-Score for trial in TM_WZ WT_SUBJECT_PROTEOMICS_MED',rowCt,stepCt,'Done');

    begin
	insert into deapp.de_subject_protein_data (
	    trial_name
	    ,protein_annotation_id
	    ,component
	    ,gene_symbol
	    ,gene_id
	    ,assay_id
	    ,subject_id
	    ,intensity
	    ,zscore
	    ,log_intensity
	    ,patient_id)
	select TrialId
               ,d.id
               ,m.probeset_id
               ,d.uniprot_id
               ,d.biomarker_id
               ,m.assay_id
               ,m.subject_id
	    --  ,decode(dataType,'R',m.intensity_value,'L',power(logBase, m.log_intensity),null)
               ,m.intensity_value as intensity  ---UAT 154 changes done on 19/03/2014
               ,(case when m.zscore < -2.5 then -2.5 when m.zscore >  2.5 then  2.5 else round(m.zscore::numeric,5) end)
               ,round(m.log_intensity::numeric,4) as log_intensity
               ,m.patient_id
	  from tm_wz.wt_subject_proteomics_med m
	       , deapp.de_protein_annotation d
         where d.peptide=m.probeset_id;
    exception
	when others then
	    perform tm_cz.cz_error_handler (jobID, procedureName, SQLSTATE, SQLERRM);
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;

    stepCt := stepCt + 1;
    get diagnostics rowCt := ROW_COUNT;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert data for trial in DEAPP DE_SUBJECT_PROTEIN_DATA',rowCt,stepCt,'Done');

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Truncate work tables in TM_WZ',0,stepCt,'Done');

    ---Cleanup OVERALL JOB if this proc is being run standalone
    if newJobFlag = 1 then
	perform tm_cz.cz_end_audit (jobID, 'SUCCESS');
    end if;

    return 1;

end;

$$;

