--
-- Name: load_tm_trial_nodes(character varying, character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE PROCEDURE TM_CZ.LOAD_TM_TRIAL_NODES (
       trial_id IN VARCHAR,
       full_name IN VARCHAR,
       currentjobid IN NUMBER DEFAULT 0,
       doReplace IN NUMBER DEFAULT 0
)
    AUTHID CURRENT_USER
AS
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

    --Audit variables
    databaseName 	VARCHAR2(100);
    procedureName 	VARCHAR2(100);
    jobID 		number(18,0);
    stepCt 		number(18,0);
    rowCt		number(18,0);
    fullName		varchar2(700);
    dbFullName		varchar2(100);
    TrialID		varchar2(100);
    tText		varchar2(2000);

    rtnCode		int;
    etlDate		date;
    newJobFlag		INTEGER(1);
    tCount		int;

    another_name	exception;

BEGIN
    EXECUTE IMMEDIATE 'alter session set NLS_NUMERIC_CHARACTERS=".,"';
    TrialID := upper(trial_id);

    --Set Audit Parameters
    databaseName := 'tm_cz';
    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    SELECT sys_context('USERENV', 'CURRENT_SCHEMA') INTO databaseName FROM dual;
    procedureName := $$PLSQL_UNIT;

    select sysdate into etlDate from dual;
    procedureName := 'LOAD_TM_TRIAL_NODES';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    IF(jobID IS NULL or jobID < 1) THEN
	newJobFlag := 1; -- True
	tm_cz.cz_start_audit (procedureName, databaseName, jobID);
    END IF;

    fullName := full_name;

    stepCt := 0;
    stepCt := stepCt + 1;
    tText := 'Test for trialId '|| trialID|| ' in tm_trial_nodes';
    tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,0,stepCt,'Done');

    select count(*) into tCount from i2b2metadata.tm_trial_nodes where trial = TrialID;
    if(tCount > 0) then
	select c_fullname into dbFullName from i2b2metadata.tm_trial_nodes where trial = TrialID;
    end if;
    
    stepCt := stepCt + 1;
    if(tCount > 0) then
	-- trialId found, check c_fullname
	if(fullName <> dbFullName) then
	    if(doReplace > 0) then
		update i2b2metadata.tm_trial_nodes
		set c_fullname = fullName
		where trial = TrialID;
		tText := 'Updated fullname for trial '||TrialID||' in tm_trial_nodes';
		tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,1,stepCt,'Done');
	    else
		raise another_name;
	    end if;
	else
	    tText := 'Already exists ' || fullname || ' for trial '||TrialID||' in tm_trial_nodes';
	    tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,1,stepCt,'Done');
	end if;
    else
	-- trialId not found, can insert
	insert into i2b2metadata.tm_trial_nodes (trial, c_fullname) values (trialId, fullName);
	tText := 'Inserted fullname for trial '||TrialID||' in tm_trial_nodes';
	tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,1,stepCt,'Done');
    end if;

    rtnCode := 1;

exception
    when another_name then
	tText := 'Found another value for fullname for trial '||TrialID||' in tm_trial_nodes';
	tm_cz.cz_error_handler (jobID, procedureName);
	tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,1,stepCt,'FAIL');
	tm_cz.cz_end_audit (jobID, 'FAIL');
	rtnCode := 16;
    when others then
    --Handle errors.
	tm_cz.cz_error_handler (jobID, procedureName);
    --End Proc
	tm_cz.cz_end_audit (jobID, 'FAIL');
	rtnCode := 16;
end;

/

