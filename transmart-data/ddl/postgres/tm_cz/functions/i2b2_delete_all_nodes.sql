--
-- Name: i2b2_delete_all_nodes(character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_delete_all_nodes(path character varying, currentjobid numeric DEFAULT 0) RETURNS numeric
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

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_delete_all_nodes';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(jobID IS NULL or jobID < 1) then
	newJobFlag := 1; -- True
	select tm_cz.cz_start_audit (procedureName, databaseName) into jobID;
    end if;

    if path != ''  and path != '%' then
	-- observation_fact
	begin
	    delete from i2b2demodata.observation_fact
	     where
		 concept_cd in (select c_basecode from i2b2metadata.i2b2 where c_fullname like path || '%' escape '`');
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
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete data for trial from I2B2DEMODATA observation_fact',rowCt,stepCt,'Done');

	--concept dimension
	begin
	    delete from i2b2demodata.concept_dimension
	     where concept_path like path || '%' escape '`';
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
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete data for trial from I2B2DEMODATA concept_dimension',rowCt,stepCt,'Done');

	--i2b2
	begin
	    delete from i2b2metadata.i2b2
	     where c_fullname like path || '%' escape '`';
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
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete data for trial from I2B2METADATA i2b2',rowCt,stepCt,'Done');

	--i2b2_secure
	begin
	    delete from i2b2metadata.i2b2_secure
	     where c_fullname like path || '%' escape '`';
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
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete data for trial from I2B2METADATA i2b2_secure',rowCt,stepCt,'Done');

	-- tm_concept_counts
	begin
	    delete from i2b2metadata.tm_concept_counts
	     where concept_path like path || '%' escape '`';
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
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete data for trial from I2B2METADATA tm_concept_counts',rowCt,stepCt,'Done');

    end if;

    return 1;

end;

$$;

