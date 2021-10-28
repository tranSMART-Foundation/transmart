--
-- Name: load_tm_trial_nodes(character varying, character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.load_tm_trial_nodes(trial_id character varying, full_name character varying, currentjobid numeric DEFAULT 0, doReplace boolean DEFAULT false) RETURNS numeric
    LANGUAGE plpgsql SECURITY DEFINER
AS $$
    /*************************************************************************
     * Copyright 2021 Oryza Bioinformatics Ltd
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
    databaseName 	VARCHAR(100);
    procedureName 	VARCHAR(100);
    jobID 			numeric(18,0);
    stepCt 			numeric(18,0);
    rowCt			numeric(18,0);
    errorNumber		character varying;
    errorMessage	character varying;
    fullName		character varying;
    dbFullName		character varying;
    trialId		character varying;

begin

    begin

	--Set Audit Parameters
	databaseName := 'tm_cz';
	procedureName := 'load_tm_trial_nodes';

	--Audit JOB Initialization
	--If Job ID does not exist, then this is a single procedure run and we need to create it
	select case when coalesce(currentjobid, -1) < 1 then tm_cz.cz_start_audit(procedureName, databaseName) else currentjobid end into jobId;

	trialId = upper(trial_id);
	fullName := full_name;

	stepCt := 0;
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Test for trialId in tm_trial_nodes',0,stepCt,'Done');

	select c_fullname from i2b2metadata.tm_trial_nodes where trial = trialId into dbFullName;

	stepCt := stepCt + 1;
	if(dbFullName IS NOT NULL) then
	    -- trialId found, check c_fullname
	    if(fullName <> dbFullName) then
		if(doReplace) then
		    update i2b2metadata.tm_trial_nodes
		    set c_fullname = fullName
		    where trial = trialId;
		    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Updated fullname for trial '||trialId||' in tm_trial_nodes',1,stepCt,'Done');
		else
		    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Found another value for fullname for trial '||trialId||' in tm_trial_nodes',1,stepCt,'FAIL');
		    return -16;
		end if;
	    else
		perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Already exists fullname for trial '||trialId||' in tm_trial_nodes',1,stepCt,'Done');
	    end if;
	else
	    -- trialId not found, can insert
	    insert into i2b2metadata.tm_trial_nodes (trial, c_fullname) values (trialId, fullName);
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Inserted fullname for trial '||trialId||' in tm_trial_nodes',1,stepCt,'Done');
	end if;

    end;
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

