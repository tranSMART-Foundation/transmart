--
-- Name: i2b2_move_study(character varying, character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_move_study(old_path character varying, new_path character varying, currentjobid numeric) RETURNS integer
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

    oldPath		varchar(2000);
    newPath		varchar(2000);
    oldPathWild		varchar(2000);
    root_node		varchar(2000);
    root_level		integer;

    oldLevel		integer;
    newLevel		integer;
    oldStudyNode	varchar(2000);
    newStudyNode	varchar(2000);
    newParent		varchar(2000);

    --Audit variables
    newJobFlag		integer;
    databaseName 	VARCHAR(100);
    procedureName 	VARCHAR(100);
    jobID 			numeric(18,0);
    stepCt 			numeric(18,0);
    rowCt			numeric(18,0);
    errorNumber		character varying;
    errorMessage	character varying;
    tText		varchar(1000);
    rtnCd		integer;

begin

    stepCt := 0;

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_move_study';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    IF(jobID IS NULL or jobID < 1)
    THEN
	newJobFlag := 1; -- True
	select tm_cz.cz_start_audit (procedureName, databaseName) into jobId;
	END IF;

    -- Fix possible missed backslashes in provided paths

    oldPath := regexp_replace('\' || old_path || '\','(\\){2,}', '\', 'g');
    newPath := regexp_replace('\' || new_path || '\','(\\){2,}', '\', 'g');

    select length(oldPath)-length(replace(oldPath,'\','')) into oldLevel;
    select length(newPath)-length(replace(newPath,'\','')) into newLevel;
    select ltrim(substr(newPath, 1,tm_cz.instr(newPath, '\',-1,2))) into newParent;

    if newLevel < 3 then
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'New path specified must contain at least 2 nodes',0,stepCt,'Msg');
	perform tm_cz.cz_error_handler (jobID, procedureName, '-1', 'Application raised error');
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
	return -16;
    end if;
    if oldLevel < 3 then
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Old path specified must contain at least 2 nodes',0,stepCt,'Msg');
	perform tm_cz.cz_error_handler (jobID, procedureName, '-1', 'Application raised error');
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
	return -16;
    end if;

    select tm_cz.parse_nth_value(oldPath, oldLevel, '\') into oldStudyNode;
    select tm_cz.parse_nth_value(newPath, newLevel, '\') into newStudyNode;

    oldPathWild := regexp_replace(oldPath, '_','`_') || '%';

    select tm_cz.parse_nth_value(newPath, 2, '\') into root_node;

    select c_hlevel into root_level
      from i2b2metadata.table_access
     where c_name = root_node;

    if old_path != ''  or old_path != '%' or new_path != ''  or new_path != '%'
    then

	--CONCEPT DIMENSION
	select count(*) into rowCt from i2b2demodata.concept_dimension
	where sourcesystem_cd = 'GSE34466';
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Count concept_dimension with sourcesystem_cd',rowCt,stepCt,'Done');

	select count(*) into rowCt from i2b2demodata.concept_dimension
	where concept_path like oldPathWild escape '`';
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Count concept_dimension with old path',rowCt,stepCt,'Done');

	select count(*) into rowCt from i2b2demodata.concept_dimension
	where concept_path like oldPathWild escape '`';
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Count concept_dimension with new path',rowCt,stepCt,'Done');

	update i2b2demodata.concept_dimension
	set concept_path = replace(concept_path, oldPath, newPath)
	where concept_path like oldPathWild escape '`';
	get diagnostics rowCt := ROW_COUNT;
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update concept_dimension with new path',rowCt,stepCt,'Done');

	update i2b2demodata.concept_dimension
	   set name_char=newStudyNode where concept_path=newPath;
	get diagnostics rowCt := ROW_COUNT;
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update concept_dimension name for new study path',rowCt,stepCt,'Done');




	--I2B2
	update i2b2metadata.i2b2
	   set c_fullname = replace(c_fullname, oldPath, newPath)
	       ,c_dimcode = replace(c_fullname, oldPath, newPath)
	       ,c_tooltip = replace(c_fullname, oldPath, newPath)
	       ,c_hlevel =  (length(replace(c_fullname, oldPath, newPath)) - COALESCE(length(replace(replace(c_fullname, oldPath, newPath), '\','')),0)) / length('\') - 2 + root_level
	 where c_fullname like oldPathWild escape '`';
	get diagnostics rowCt := ROW_COUNT;
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update i2b2 with new path',rowCt,stepCt,'Done');

	update i2b2metadata.i2b2
	   set c_name=newStudyNode where c_fullname=newPath;
	get diagnostics rowCt := ROW_COUNT;
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update i2b2 name for new study node',rowCt,stepCt,'Done');

	update i2b2metadata.i2b2_secure
	   set c_fullname = replace(c_fullname, oldPath, newPath)
	       ,c_dimcode = replace(c_fullname, oldPath, newPath)
	       ,c_tooltip = replace(c_fullname, oldPath, newPath)
	       ,c_hlevel =  (length(replace(c_fullname, oldPath, newPath)) - COALESCE(length(replace(replace(c_fullname, oldPath, newPath), '\','')),0)) / length('\') - 2 + root_level
	 where c_fullname like oldPathWild escape '`';
	get diagnostics rowCt := ROW_COUNT;
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update i2b2_secure with new path',rowCt,stepCt,'Done');

	update i2b2metadata.i2b2
	   set c_name=newStudyNode where c_fullname=newPath;
	get diagnostics rowCt := ROW_COUNT;
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update i2b2_secure name for new study node',rowCt,stepCt,'Done');




	--	tm_concept_counts

	update i2b2metadata.tm_concept_counts
	   set concept_path = replace(concept_path, oldPath, newPath)
	       ,parent_concept_path = replace(parent_concept_path, oldPath, newPath)
	 where concept_path like oldPathWild escape '`';
	get diagnostics rowCt := ROW_COUNT;
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update tm_concept_counts with new path',rowCt,stepCt,'Done');
	update i2b2metadata.tm_concept_counts
	   set parent_concept_path = newParent
	 where concept_path = newPath;
	get diagnostics rowCt := ROW_COUNT;
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update tm_concept_counts study parent',rowCt,stepCt,'Done');


	-- No other tm_concept_counts update needed for the study node

	--	fill in any upper levels

	select tm_cz.i2b2_fill_in_tree(null, newPath, jobID) into rtnCd;
	if(rtnCd <> 1) then
	    tText := 'Failed to fill in tree '|| newPath;
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,0,stepCt,'Message');
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
	end if;
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Fill in upper levels of tree for new path',0,stepCt,'Done');
    end if;



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

