--
-- Name: nz_i2b2_copy_to_stage(character varying, character varying, bigint); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.nz_i2b2_copy_to_stage(character varying, character varying, bigint) RETURNS integer
    LANGUAGE plpgsql IMMUTABLE SECURITY DEFINER
AS $$
    /*************************************************************************
     * Copyright 2008-2012 Janssen Research & Development, LLC.
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

    --	Alias for parameters

    trial_id  alias for $1;
    data_type alias for $2;
    currentJobID alias for $3;
    TrialId 	varchar(200);
    msgText		varchar(2000);
    dataType	varchar(50);

    tText			varchar(2000);
    tExists 		integer;
    tCount			integer;
    source_table	varchar(50);
    release_table	varchar(50);
    vSNP 			integer;
    rowCt			bigint;
    v_sqlerrm		varchar(1000);

    --Audit variables
    newJobFlag integer;
    databaseName VARCHAR(100);
    procedureName VARCHAR(100);
    jobID numeric(18,0);
    stepCt numeric(18,0);

    r_stage_table	record;


begin

    TrialID := upper(trial_id);
    dataType := upper(data_type);

    stepCt := 0;
    tCount := 0;

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'nz_i2b2_copy_to_stage';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(jobID IS NULL or jobID < 1) then
	newJobFlag := 1; -- True
	jobId := tm_cz.czx_start_audit (procedureName, databaseName);
    end if;

    stepCt := stepCt + 1;
    perform tm_cz.czx_write_audit(jobId,databaseName,procedureName,'Starting ' || procedureName,0,stepCt,'Done');

    stepCt := stepCt + 1;
    msgText := 'Extracting trial: ' || TrialId;
    perform tm_cz.czx_write_audit(jobId,databaseName,procedureName, msgText,0,stepCt,'Done');

    if TrialId = null then
	stepCt := stepCt + 1;
	perform tm_cz.czx_write_audit(jobId,databaseName,procedureName,'TrialID missing',0,stepCt,'Done');
	return 16;
    end if;

    for r_stage_table in
	select upper(table_owner) as table_owner
	       ,upper(table_name) as table_name
	       ,upper(study_specific) as study_specific
	       ,where_clause
	       ,upper(stage_table_name) as stage_table_name
	  from tm_cz.migrate_tables
	 where tm_cz.instr(dataType,data_type) > 0
	       loop

	    source_table := r_stage_table.table_owner || '.' || r_stage_table.table_name;
	    release_table := 'tm_stage.' || r_stage_table.stage_table_name;
	    stepCt := stepCt + 1;
	    perform tm_cz.czx_write_audit(jobId,databaseName,procedureName,'Processing ' || source_table,0,stepCt,'Done');
	    tCount := tCount + 1;

	    if r_stage_table.study_specific = 'Y' then
		tText := 'delete from ' || release_table || ' where release_study = ' || '''' || TrialId || '''';
		execute immediate tText;
		rowCt := ROW_COUNT;
		stepCt := stepCt + 1;
		perform tm_cz.czx_write_audit(jobId,databaseName,procedureName,'Deleted study from ' || release_table,rowCt,stepCt,'Done');

		tText := 'insert into ' || release_table || ' select st.*,' || '''' || TrialId || '''' || ' from ' || source_table || ' st ' ||
		    replace(r_stage_table.where_clause,'TrialId','''' || TrialId || '''');
		execute immediate tText ;
		rowCt := ROW_COUNT;
		stepCt := stepCt + 1;
		perform tm_cz.czx_write_audit(jobId,databaseName,procedureName,'Inserted study into ' || release_table,rowCt,stepCt,'Done';
	    else
		tText := 'truncate table ' || release_table;
		stepCt := stepCt + 1;
		execute immediate tText;
		perform tm_cz.czx_write_audit(jobId,databaseName,procedureName,'Truncated '|| release_table,0,stepCt,'Done');
		tText := 'insert into ' || release_table || ' select st.* from ' || source_table || ' st ';
		execute immediate(tText);
		rowCt := ROW_COUNT;
		stepCt := stepCt + 1;
		perform tm_cz.czx_write_audit(jobId,databaseName,procedureName,'Inserted all data into ' || release_table,rowCt,stepCt,'Done');
	    end if;

    end loop;

    if tCount = 0 then
	stepCt := stepCt + 1;
	perform tm_cz.czx_write_audit(jobId,databaseName,procedureName,'data_type invalid: '|| data_type,0,stepCt,'Done');
	Return 16;
    end if;
    stepCt := stepCt + 1;
    perform tm_cz.czx_write_audit(jobId,databaseName,procedureName,'End '||procedureName,0,stepCt,'Done');

    ---Cleanup OVERALL JOB if this proc is being run standalone
    if newJobFlag = 1 then
	perform tm_cz.czx_end_audit (jobID, 'SUCCESS');
    end if;

    return 0;

exception
    when others then
	v_sqlerrm := substr(SQLERRM,1,1000);
	raise notice 'error: %', v_sqlerrm;
	--Handle errors.
	perform tm_cz.czx_error_handler (jobID, procedureName,v_sqlerrm);
	--End Proc
	perform tm_cz.czx_end_audit (jobID, 'FAIL');
	return 16;

end;
$$;

