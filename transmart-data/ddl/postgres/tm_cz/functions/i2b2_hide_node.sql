--
-- Name: i2b2_hide_node(character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_hide_node(path character varying) RETURNS numeric
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
    rtnCd		integer;

begin

    stepCt := 0;
    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_hide_node';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(jobID IS NULL or jobID < 1) then
	newJobFlag := 1; -- True
	select tm_cz.cz_start_audit (procedureName, databaseName) into jobID;
    end if;

    if path = ''  or path = '%' or path_name = '' then
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Path or Path name missing, no action taken',0,stepCt,'Done');
	return 1;
	end if;

    begin
	update i2b2metadata.i2b2 b
	   set c_visualattributes=substr(b.c_visualattributes,1,1) || 'H' || substr(b.c_visualattributes,3,1)
	 where c_fullname like path || '%' escape '`';
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
	    get diagnostics rowCt := ROW_COUNT;
    end;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Nodes hidden in i2b2metadata.i2b2',rowCt,stepCt,'Done');


    begin
	delete from i2b2metadata.tm_concept_counts
	 where concept_path like path || '%' escape '`';
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
	    get diagnostics rowCt := ROW_COUNT;
    end;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Deleted hidden nodes from i2b2metadata.tm_concept_counts',rowCt,stepCt,'Done');

    --	reload i2b2_secure for hidden nodes

    select tm_cz.load_security_data(jobId) into rtnCd;

    return rtnCd;

end;

$$;

