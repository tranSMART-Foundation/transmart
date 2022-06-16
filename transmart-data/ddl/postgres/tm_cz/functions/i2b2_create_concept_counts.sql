--
-- Name: i2b2_create_concept_counts(character varying, character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_create_concept_counts(trialid character varying, path character varying, currentjobid numeric DEFAULT 0) RETURNS numeric
    LANGUAGE plpgsql SECURITY DEFINER
AS $$
    /*************************************************************************
     * Copyright 2021 Axiomedix Inc.
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
     ******************************************************************/

    declare

    --Audit variables
    newJobFlag		integer;
    databaseName 	VARCHAR(100);
    procedureName 	VARCHAR(100);
    jobID 		numeric(18,0);
    stepCt 		numeric(18,0);
    rowCt		numeric(18,0);
    errorNumber		character varying;
    errorMessage	character varying;
    tExplain 		text;

    curRecord		RECORD;
    v_sqlstring		text = '';

    tableName		character varying;

    pathEscaped		character varying;
    topPath		character varying;
    testPath		character varying(700);
begin

    tableName := '@';

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_create_concept_counts';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(jobID IS NULL or jobID < 1) then
	newJobFlag := 1; -- True
	select tm_cz.cz_start_audit (procedureName, databaseName) into jobID;
    end if;

    stepCt := 0;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Starting with trialid '||trialid||' path '||path,0,stepCt,'Done');

    -- find missing path from trialId
    if(path is null or path = '') then
	select c_fullname from i2b2metadata.tm_trial_nodes where trial = trialid into path;
	get diagnostics rowCt := ROW_COUNT;
	if(rowCt < 1) then
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'No path defined for trial '||trialid,rowCt,stepCt,'FAIL');
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
	    end if;
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Found path '||path||' for trial '||trialid,rowCt,stepCt,'Done');

    elsif(trialid != 'I2B2') then		-- check path provided against known path in tm_trial_nodes
	select count(*) from i2b2metadata.tm_trial_nodes into rowCt;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Count of all trial nodes', rowCt,stepCt,'Log');
	select count(*) from i2b2metadata.tm_trial_nodes where trial = trialid into rowCt;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Count of trial nodes for trial '||trialid,rowCt,stepCt,'Log');
	begin
	    select c_fullname from i2b2metadata.tm_trial_nodes where trial = trialid into testPath;
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
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Testing tm_trial_nodes '||trialid,rowCt,stepCt,'Log');
	if(rowCt < 1) then
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'No path for trial '||trialid,rowCt,stepCt,'FAIL');
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
	elsif(path != testPath) then
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Unmatched path for trial '||trialid||' found '||testPath,rowCt,stepCt,'FAIL');
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
	end if;
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Found path '||path||' for trial '||trialid,rowCt,stepCt,'Done');
    end if;
    
    -- may need to replace \ to \\ and ' to '' in the path parameter

    topPath := path;
    topPath := replace(replace(topPath,'\','\\'),'''','''''');
    pathEscaped := replace(topPath, '_', '`_');

    select count(*) from i2b2metadata.tm_concept_counts
	 where concept_path like pathEscaped || '%' escape '`' into rowCt;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Before delete: tm_concept_counts records below concept_path ' || path,rowCt,stepCt,'Done');

    begin
	delete from i2b2metadata.tm_concept_counts
	 where concept_path like pathEscaped || '%' escape '`';
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete counts for trial from I2B2METADATA tm_concept_counts',rowCt,stepCt,'Done');

    select count(*) from i2b2metadata.tm_concept_counts
	 where concept_path = topPath into rowCt;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'After delete for concept_path ' || path || ' records remaining',rowCt,stepCt,'Done');

    -- code based on i2b2metadata functions for populating the i2b2 totalnum table
    -- i2b2 temp tables substituted by temp_* tables in tm_cz

    -- clear the working tables

    execute ('truncate table tm_cz.temp_concept_path');
    execute ('truncate table tm_cz.temp_dim_count_ont');
    execute ('truncate table tm_cz.temp_dim_ont_with_folders');
    execute ('truncate table tm_cz.temp_final_counts_by_concept');
    execute ('truncate table tm_cz.temp_ont_pat_visit_dims');
    execute ('truncate table tm_cz.temp_path_counts');
    execute ('truncate table tm_cz.temp_path_to_num');
    
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Truncated working tables',0,stepCt,'Done');

    for curRecord IN 
        select distinct upper(c_table_name) as sqltext
        from i2b2metadata.table_access 
        where c_visualattributes like '%A%' 
    LOOP 
	IF tableName='@' OR tableName=curRecord.sqltext THEN
            v_sqlstring := 'select tm_cz.i2b2_pat_count_visits( ''' || curRecord.sqltext || ''' , ''i2b2demodata'', ''' || topPath || ''' , '||jobID||')';
            execute v_sqlstring;
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Counted visits in ' || curRecord.sqltext,0,stepCt,'Done');
            
            v_sqlstring := 'select tm_cz.i2b2_pat_count_dimensions( ''' || curRecord.sqltext || ''' , ''i2b2demodata'', ''observation_fact'',  ''concept_cd'', ''concept_dimension'', ''concept_path'', ''' || topPath || '''  , '||jobID||')';
            execute v_sqlstring;
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Counted concepts in ' || curRecord.sqltext,0,stepCt,'Done');
            
            v_sqlstring := 'select tm_cz.i2b2_pat_count_dimensions( ''' || curRecord.sqltext || ''' , ''i2b2demodata'', ''observation_fact'' ,  ''provider_id'', ''provider_dimension'', ''provider_path'', ''' || topPath || '''  , '||jobID||')';
            execute v_sqlstring;
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Counted providers in ' || curRecord.sqltext,0,stepCt,'Done');
            
            v_sqlstring := 'select tm_cz.i2b2_pat_count_dimensions( ''' || curRecord.sqltext || ''' , ''i2b2demodata'', ''observation_fact'' ,  ''modifier_cd'', ''modifier_dimension'', ''modifier_path'', ''' || topPath || '''  , '||jobID||')';
            execute v_sqlstring;
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Counted modifiers in ' || curRecord.sqltext,0,stepCt,'Done');

--	    execute 'update i2b2metadata.table_access set c_totalnum=(select c_totalnum from ' || curRecord.sqltext || ' x where x.c_fullname=table_access.c_fullname)';
        END IF;

    END LOOP;

    begin
--    for tExplain in
--    EXPLAIN (ANALYZE, VERBOSE, BUFFERS)
	update i2b2metadata.i2b2
	   set c_visualattributes = substr(c_visualattributes,1,1) || 'H' || substr(c_visualattributes,3,1)
	 where c_fullname like pathEscaped || '%' escape '`'
	       and (not exists
		    (select 1 from i2b2metadata.tm_concept_counts nc
		      where c_fullname = nc.concept_path)
		      or
		      exists
		      (select 1 from i2b2metadata.tm_concept_counts zc
			where c_fullname = zc.concept_path
			  and zc.patient_count = 0)
	       )
	       and c_name != 'SECURITY'
--	LOOP
--	    raise notice 'explain: %', tExplain;
--	END LOOP
	;
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Nodes hidden with missing/zero counts for trial in I2B2METADATA tm_concept_counts',rowCt,stepCt,'Done');

    --    update i2b2metadata.table_access set c_totalnum=null where c_totalnum=0;

    -- tables populated in i2b2_pat_count_visits
    stepCt := stepCt + 1;
    select count(*) from tm_cz.temp_ont_pat_visit_dims into rowCt;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Visit table temp_ont_pat_visit_dims rows:',rowCt,stepCt,'Done');

    -- tables populated in i2b2_pat_count_dimensions
    stepCt := stepCt + 1;
    select count(*) from tm_cz.temp_dim_count_ont into rowCt;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Table temp_dim_count_ont rows:',rowCt,stepCt,'Done');
    stepCt := stepCt + 1;
    select count(*) from tm_cz.temp_dim_ont_with_folders into rowCt;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Table temp_dim_ont_with_folders rows:',rowCt,stepCt,'Done');
    stepCt := stepCt + 1;
    select count(*) from tm_cz.temp_path_to_num into rowCt;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Table temp_path_to_num rows:',rowCt,stepCt,'Done');
    stepCt := stepCt + 1;
    select count(*) from tm_cz.temp_concept_path into rowCt;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Table temp_concept_path rows:',rowCt,stepCt,'Done');
    stepCt := stepCt + 1;
    select count(*) from tm_cz.temp_path_counts into rowCt;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Table temp_path_counts rows:',rowCt,stepCt,'Done');
    stepCt := stepCt + 1;
    select count(*) from tm_cz.temp_final_counts_by_concept into rowCt;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Table temp_final_counts_by_concept rows:',rowCt,stepCt,'Done');


    return 1;

end; 
$$
  VOLATILE
  COST 100;
