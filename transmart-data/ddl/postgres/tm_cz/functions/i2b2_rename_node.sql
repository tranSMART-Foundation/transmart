--
-- Name: i2b2_rename_node(character varying, character varying, character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_rename_node(trial_id character varying, old_node character varying, new_node character varying, currentjobid numeric DEFAULT 0) RETURNS numeric
    LANGUAGE plpgsql
AS $$
    declare

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

    --Audit variables
    newJobFlag integer;
    databaseName varchar(100);
    procedureName varchar(100);
    jobID bigint;
    stepCt bigint;
    rowCt bigint;
    errorNumber    character varying;
    errorMessage  character varying;
    rtnCd integer;
    TrialID		varchar(100);

begin

    rtnCd := 1;
    TrialID := upper(trial_id);

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_rename_node';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it

    if(jobID IS NULL or jobID < 1) then
	newJobFlag := 1; -- True
	select tm_cz.cz_start_audit (procedureName, databaseName) into jobID;
    end if;

    stepCt := 0;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Start i2b2_rename_node',0,stepCt,'Done');

    if old_node != ''  and old_node != '%' and new_node != ''  and new_node != '%' then

	--  Update tm_concept_counts paths

	begin
	    update i2b2metadata.tm_concept_counts cc
	       set concept_path = replace(cc.concept_path, '\' || old_node || '\', '\' || new_node || '\')
		   ,parent_concept_path = replace(cc.parent_concept_path, '\' || old_node || '\', '\' || new_node || '\')
	     where cc.concept_path in
		   (select cd.concept_path
		      from i2b2demodata.concept_dimension cd
		     where cd.sourcesystem_cd = trial_id
		       and cd.concept_path like '%' || old_node || '%');
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
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update tm_concept_counts with new path',rowCt,stepCt,'Done');

	--Update path in i2b2_tags
	begin
	    update i2b2metadata.i2b2_tags t
	       set path = replace(t.path, '\' || old_node || '\', '\' || new_node || '\')
	     where t.path in
		   (select cd.concept_path from i2b2demodata.concept_dimension cd
		     where cd.sourcesystem_cd = trial_id
		       and cd.concept_path like '%\' || old_node || '\%' escape '`');
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
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update i2b2_tags with new path',rowCt,stepCt,'Done');

	--Update specific name
	--update concept_dimension
	--  set name_char = new_node
	--  where name_char = old_node
	--    and sourcesystem_cd = trial_id;

	--Update all paths
	begin
	    update i2b2demodata.concept_dimension
	       set concept_PATH = replace(concept_path, '\' || old_node || '\', '\' || new_node || '\')
		   ,name_char=(CASE WHEN name_char=old_node THEN new_node ELSE name_char END)
	     where sourcesystem_cd = trial_id
		   and concept_path like '%\' || old_node || '\%' escape '`';
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
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update concept_dimension with new path',rowCt,stepCt,'Done');

	--Update all paths, added updates to c_dimcode and c_tooltip instead of separate pass
	begin
	    update i2b2metadata.i2b2
	       set c_fullname = replace(c_fullname, '\' || old_node || '\', '\' || new_node || '\')
		   ,c_dimcode = replace(c_dimcode, '\' || old_node || '\', '\' || new_node || '\')
		   ,c_tooltip = replace(c_tooltip, '\' || old_node || '\', '\' || new_node || '\')
		   ,c_name = (CASE WHEN c_name=old_node THEN new_node ELSE c_name END)
	     where sourcesystem_cd = trial_id
		   and c_fullname like '%\' || old_node || '\%' escape '`';
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
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update i2b2 with new path',rowCt,stepCt,'Done');

	select tm_cz.i2b2_load_security_data(TrialId,jobID) into rtnCd;
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update i2b2_secure with new path',rowCt,stepCt,'Done');

	return rtnCd;

    end if;
end;

$$;

