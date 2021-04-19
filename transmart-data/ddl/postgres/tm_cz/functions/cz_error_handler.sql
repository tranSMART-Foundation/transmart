--
-- Name: cz_error_handler(numeric, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.cz_error_handler(jobid numeric, procedurename character varying, errornumber character varying, errormessage character varying, errorstack character varying) RETURNS integer
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

    databaseName VARCHAR(100);
    errorText text;

    stepNo numeric(18,0);

begin
    --Get DB Name
    select database_name into databaseName
      from tm_cz.cz_job_master
     where job_id=jobID;

    --Get Latest Step
    select max(step_number) into stepNo from tm_cz.cz_job_audit where job_id = jobID;

    --Get all error info, passed in as parameters, only available from EXCEPTION block
    --errorNumber := SQLSTATE;
    --errorMessage := SQLERRM;
    --errorStack := DIAGNOSTICS PG_CONTEXT (in exception handler)

    --	No corresponding functionality in PostgreSQL
    --errorBackTrace := dbms_utility.format_error_backtrace;

    --Update the audit step for the error
    perform tm_cz.cz_write_audit(jobID, databaseName,procedureName, 'Job Failed: See error log for details',1, stepNo, 'FAIL');

    --write out the error info
    perform tm_cz.cz_write_error(jobID, errorNumber, errorMessage, errorStack, errorText);

    return 1;

end;

$$;

--
-- Name: cz_error_handler(numeric, character varying, character varying, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.cz_error_handler(jobid numeric, procedurename character varying, errornumber character varying, errormessage character varying) RETURNS integer
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

    databaseName VARCHAR(100);
    errorText text;

    stepNo numeric(18,0);

begin

    return tm_cz.cz_error_handler(jobid, procedurename, errornumber, errormessage, '');

end;

$$
