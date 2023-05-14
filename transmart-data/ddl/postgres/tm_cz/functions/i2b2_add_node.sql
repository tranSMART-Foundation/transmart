--
-- Name: i2b2_add_node(character varying, character varying, character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_add_node(trialid character varying, path character varying, path_name character varying, currentjobid numeric) RETURNS integer
    LANGUAGE plpgsql SECURITY DEFINER
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

    root_node		varchar(2000);
    root_level	integer;

    --Audit variables
    newJobFlag		integer;
    databaseName 	VARCHAR(100);
    procedureName 	VARCHAR(100);
    jobID 			numeric(18,0);
    stepCt 			numeric(18,0);
    rowCt			numeric(18,0);
    errorNumber		character varying;
    errorMessage	character varying;

begin

    stepCt := 0;

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_add_node';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(jobID IS NULL or jobID < 1) then
	newJobFlag := 1; -- True
	select tm_cz.cz_start_audit (procedureName, databaseName) into jobId;
    end if;

    select tm_cz.parse_nth_value(path, 2, '\') into root_node;

    select c_hlevel into root_level
      from i2b2metadata.table_access
     where c_name = root_node;

    if path = ''  or path = '%' or path_name = '' then
        stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Path or Path name missing, no action taken',0,stepCt,'Done');
	return 1;
    end if;


    --Delete existing data.

    delete from i2b2demodata.observation_fact
     where concept_cd in (select c_basecode from i2b2metadata.i2b2 where c_fullname = path);
    get diagnostics rowCt := ROW_COUNT;
    if (rowCt > 0) then
        stepCt := stepCt + 1;
        perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Deleted any concepts for path from I2B2DEMODATA observation_fact',rowCt,stepCt,'Done');
    end if;

    --concept dimension
    delete from i2b2demodata.concept_dimension
     where concept_path = path;
    get diagnostics rowCt := ROW_COUNT;
    if (rowCt > 0) then
        stepCt := stepCt + 1;
        perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Deleted any concepts for path from I2B2DEMODATA concept_dimension',rowCt,stepCt,'Done');
    end if;

    --i2b2
    delete from i2b2metadata.i2b2
     where c_fullname = path;
    get diagnostics rowCt := ROW_COUNT;
    if (rowCt > 0) then
        stepCt := stepCt + 1;
        perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Deleted path from I2B2METADATA i2b2',rowCt,stepCt,'Done');
    end if;

    --	Insert new node

    --concept dimension
    insert into i2b2demodata.concept_dimension
		(concept_cd
		,concept_path
		,name_char
		,update_date
		,download_date
		,import_date
		,sourcesystem_cd)
    VALUES (
	'TM' || cast(nextval('i2b2demodata.concept_id') as varchar)
	,path
	,path_name
	,current_timestamp
	,current_timestamp
	,current_timestamp
	,TrialID);
    get diagnostics rowCt := ROW_COUNT;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Inserted concept for path into I2B2DEMODATA concept_dimension',rowCt,stepCt,'Done');

    --i2b2
    insert into i2b2metadata.i2b2
		(c_hlevel
		,c_fullname
		,c_name
		,c_visualattributes
		,c_synonym_cd
		,c_facttablecolumn
		,c_tablename
		,c_columnname
		,c_dimcode
		,c_tooltip
		,update_date
		,download_date
		,import_date
		,sourcesystem_cd
		,c_basecode
		,c_operator
		,c_columndatatype
		,c_comment
		,m_applied_path)
    select
	(length(concept_path) - coalesce(length(replace(concept_path, '\','')),0)) / length('\') - 2 + root_level
	,concept_path
	,name_char
	,'FA'
	,'N'
	,'CONCEPT_CD'
	,'CONCEPT_DIMENSION'
	,'CONCEPT_PATH'
	,concept_path
	,concept_path
	,current_timestamp
	,current_timestamp
	,current_timestamp
	,sourcesystem_cd
	,concept_cd
	,'LIKE'
	,'T'
	,case when TrialID is null then null else 'trial:' || TrialID end
	,'@'
      from i2b2demodata.concept_dimension
     where concept_path = path;
    get diagnostics rowCt := ROW_COUNT;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Inserted path into I2B2METADATA i2b2',rowCt,stepCt,'Done');

    ---Cleanup OVERALL JOB if this proc is being run standalone
    if newJobFlag = 1 then
	perform tm_cz.cz_end_audit (jobID, 'SUCCESS');
    end if;

    return 1;

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

$$;

