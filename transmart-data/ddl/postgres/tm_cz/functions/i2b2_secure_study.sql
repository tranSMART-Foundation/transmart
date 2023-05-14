--
-- Name: i2b2_secure_study(text, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_secure_study(trial_id text, currentjobid numeric DEFAULT NULL::bigint) RETURNS integer
    LANGUAGE plpgsql
AS $$
    declare

    /*************************************************************************
     * Copyright 2008-2012 Janssen Research d, LLC.
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
    rtnCd integer;

    v_bio_experiment_id	bigint;
    pExists				integer;
    TrialId				varchar(100);

begin

    TrialId := upper(trial_id);

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_secure_study';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(coalesce(jobID::text, '') = '' or jobID < 1) then
	newJobFlag := 1; -- True
	select tm_cz.cz_start_audit (procedureName, databaseName) into jobID;
    end if;

    stepCt := 0;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobID,databaseName,procedureName,'Start ' || procedureName,0,stepCt,'Done');

    --	create security records in observation_fact

    perform tm_cz.i2b2_create_security_for_trial(TrialId, 'Y', 2, jobID);

    --	load i2b2_secure

    select tm_cz.i2b2_load_security_data(TrialID,jobID) into rtnCd;
    if(rtnCd <> 1) then
        stepCt := stepCt + 1;
        perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Failed to load security data',0,stepCt,'Message');
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
	return -16;
    end if;

    --	check if entry exists for study in bio_experiment

    select count(*) into pExists
      from biomart.bio_experiment
     where accession = TrialId;

    if pExists = 0 then
	insert into biomart.bio_experiment (
	    title
	    ,accession
	    ,etl_id)
	'Metadata not available'
	,TrialId
	,'METADATA:' || TrialId;
	get diagnostics rowCt := ROW_COUNT;
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobID,databaseName,procedureName,'Insert trial/study into biomart.bio_experiment',rowCt,stepCt,'Done');
	--commit;
    end if;

    select bio_experiment_id into v_bio_experiment_id
      from biomart.bio_experiment
     where accession = TrialId;

    insert into searchapp.search_secure_object
		(bio_data_id
		,display_name
		,data_type
		,bio_data_unique_id)
    select v_bio_experiment_id
	   ,tm_cz.parse_nth_value(md.c_fullname,2,'\') || ' - ' || md.c_name as display_name
	   ,'BIO_CLINICAL_TRIAL' as data_type
	   ,'EXP:' || TrialId as bio_data_unique_id
      from i2b2metadata.i2b2 md
     where md.sourcesystem_cd = TrialId
       and md.c_hlevel =
	   (select min(x.c_hlevel) from i2b2metadata.i2b2 x
	     where x.sourcesystem_cd = TrialId)
       and not exists
	   (select 1 from searchapp.search_secure_object so
	     where v_bio_experiment_id = so.bio_data_id);
    get diagnostics rowCt := ROW_COUNT;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobID,databaseName,procedureName,'Inserted trial/study into SEARCHAPP search_secure_object',rowCt,stepCt,'Done');
    --commit;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobID,databaseName,procedureName,'End ' || procedureName,rowCt,stepCt,'Done');
    --commit;

    ---Cleanup OVERALL JOB if this proc is being run standalone
    if newJobFlag = 1 then
	perform tm_cz.cz_end_audit (jobID, 'SUCCESS');
    end if;

exception
    when others then
	raise notice 'Error % %', SQLSTATE, SQLERRM;
    --Handle errors.
	perform tm_cz.cz_error_handler (jobID, procedureName, SQLSTATE, SQLERRM);

    --End Proc
	perform tm_cz.cz_end_audit (jobID, 'FAIL');

end;

$$;

