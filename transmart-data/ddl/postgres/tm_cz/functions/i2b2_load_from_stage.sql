--
-- Name: i2b2_load_from_stage(character varying, character varying, bigint); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_load_from_stage(trial_id character varying, data_type character varying, currentJobID bigint) RETURNS integer
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

    TrialId 		varchar(200);
    msgText			varchar(2000);
    dataType		varchar(50);

    tText			varchar(2000);
    tExists 		integer;
    source_table	varchar(50);
    release_table	varchar(50);
    tableOwner		varchar(50);
    tableName		varchar(50);
    vSNP 			integer;
    topNode			varchar(1000);
    rootNode		varchar(1000);
    tPath			varchar(1000);
    pExists			integer;
    pCount			integer;
    rowCt			bigint;
    bslash			char(1);

    --Audit variables
    newJobFlag integer;
    databaseName VARCHAR(100);
    procedureName VARCHAR(100);
    jobID numeric(18,0);
    stepCt numeric(18,0);
    v_sqlerrm		varchar(1000);

    r_stage_table	record;
    r_stage_columns record;
    rtnCd	    integer;

begin

    TrialID := upper(trial_id);
    dataType := upper(data_type);

    stepCt := 0;
    pCount := 0;
    bslash := '\\';

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_load_from_stage';

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
    select tm_cz.czx_write_audit(jobId,databaseName,procedureName, msgText,0,stepCt,'Done');

    if TrialId is null then
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

	    pCount := pCount + 1;
	    --	setup variables

	    source_table := r_stage_table.table_owner || '.' || r_stage_table.table_name;
	    release_table := 'tm_stage.' || r_stage_table.stage_table_name;
	    tableName := r_stage_table.table_name;
	    tableOwner := r_stage_table.table_owner;
	    stepCt := stepCt + 1;
	    perform tm_cz.czx_write_audit(jobId,databaseName,procedureName,'Processing ' || source_table,0,stepCt,'Done');

	    if r_stage_table.study_specific = 'N' then
		--	truncate target table
	    tText := 'truncate table ' || source_table;
		execute immediate tText;
		stepCt := stepCt + 1;
		perform tm_cz.czx_write_audit(jobId,databaseName,procedureName,'Truncated '|| source_table,0,stepCt,'Done');
		--	insert from staged source into target
		tText := 'insert into ' || source_table || ' select st.* from ' || release_table || ' st ';
		execute immediate tText;
		rowCt := ROW_COUNT;
		stepCt := stepCt + 1;
		perform tm_cz.czx_write_audit(jobId,databaseName,procedureName,'Inserted all data into ' || source_table,rowCt,stepCt,'Done');
	    else
		tText := 'delete from ' || source_table || ' st ' || replace(r_stage_table.where_clause,'TrialId','''' || TrialId || '''');
		execute immediate tText;
		rowCt := ROW_COUNT;
		stepCt := stepCt + 1;
		perform tm_cz.czx_write_audit(jobId,databaseName,procedureName,'Deleted study from ' || source_table,ROW_COUNT,stepCt,'Done');
		tText := 'insert into ' || source_table || ' select ';

		--	get list of columns in order

		for r_stage_columns in
		    select attname as column_name
		    from _v_relation_column
		    where name=upper(tablename)
		    order by attnum asc
		    loop
		    --	insert by column for study_specific
		    tText := tText || ' st.' || r_stage_columns.column_name || ',';
		end loop;

		tText := trim(trailing ',' from tText) || ' from ' || release_table || ' st ' || ' where st.release_study = ' || '''' || TrialId || '''';
		execute immediate tText;
		rowCt := ROW_COUNT;
		stepCt := stepCt + 1;
		perform tm_cz.czx_write_audit(jobId,databaseName,procedureName,'Inserted study into ' || source_table,rowCt,stepCt,'Done');
	    end if;

    end loop;

    if pCount = 0 then
	stepCt := stepCt + 1;
	perform tm_cz.czx_write_audit(jobId,databaseName,procedureName,'No staged data for study',0,stepCt,'Done');
	return 16;
    end if;

    --	if CLINICAL data, add root node if needed and fill in tree for any top nodes

    if tm_cz.instr(dataType,'CLINICAL') > 0 then

	--	get topNode for study

	select min(c_fullname) into topNode
	from i2b2metadata.i2b2
	where sourcesystem_cd = TrialId;

	if topNode is null then
	    stepCt := stepCt + 1;
	    perform tm_cz.czx_write_audit(jobId,databaseName,procedureName,'Unable to get topNode for study',0,stepCt,'Done');
	    return 16;
	end if;

	-- Get rootNode from topNode

	rootNode := replace(substr(topNode,1,tm_cz.instr(topNode,bslash,2)),bslash,'');

	select count(*) into pExists
	  from i2b2metadata.table_access
	 where c_name = rootNode;

	select count(*) into pCount
	  from i2b2metadata.i2b2
	 where c_name = rootNode;

	if pExists = 0 or pCount = 0 then
	    perform tm_cz.i2b2_add_root_node(rootNode, jobId);
	end if;

	--	Add any upper level nodes as needed, trim off study name because it's already in i2b2

	tPath := substr(topNode, 1,tm_cz.instr(topNode,bslash,-2,1));
	pCount := length(tPath) - length(replace(tPath,bslash,''));

	if pCount > 2 then
	    stepCt := stepCt + 1;
	    perform tm_cz.czx_write_audit(jobId,databaseName,procedureName,'Adding upper-level nodes',0,stepCt,'Done');
	    select tm_cz.i2b2_fill_in_tree(null, tPath, jobId) into rtnCd;
	    if(rtnCd <> 1) then
                tText := 'Failed to fill in tree '|| tPath;
                perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,0,stepCt,'Message');
                perform tm_cz.cz_end_audit (jobID, 'FAIL');
                return -16;
            end if;
	end if;

	select tm_cz.i2b2_load_security_data(TrialID, jobId) into rtnCd;
	if(rtnCd <> 1) then
            stepCt := stepCt + 1;
            perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Failed to load security data',0,stepCt,'Message');
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
	end if;
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

