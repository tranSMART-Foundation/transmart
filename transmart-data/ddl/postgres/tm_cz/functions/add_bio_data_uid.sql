--
-- Name: add_bio_data_uid(character varying, character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.add_bio_data_uid(bioUid character varying, bioType character varying, bioId integer) RETURNS integer
    LANGUAGE plpgsql SECURITY DEFINER
AS $$

    /*************************************************************************
     * Copyright 2020 Oryza Bioinformatcs Ltd
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
     **************************************************************************/

    declare

    msgTxt		character varying;

    --Audit variables
    databaseName	VARCHAR(100);
    procedureName	VARCHAR(100);
    jobId 		numeric(18,0);
    stepCt 		numeric(18,0);
    rowCt		numeric(18,0);
    errorNumber		character varying;
    errorMessage	character varying;
    rtnCd		integer;

begin

    stepCt := 0;

    --Set Audit Parameters

    databaseName := 'tm_cz';
    procedureName := 'add_bio_data_uid';

    select tm_cz.cz_start_audit (procedureName, databaseName) into jobId;

    select bio_data_id from biomart.bio_data_uid where unique_id = bioUid into rtnCd;
    stepCt := stepCt + 1;
    if rtnCd > 0 then
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Species found in bio_data_uid',rtnCd,stepCt,'Done');
    else
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Adding species to bio_data_uid',0,stepCt,'Done');
	insert into biomart.bio_data_uid(
	    bio_data_id
	    ,unique_id
	    ,bio_data_type)
	values(
	    bioId
	    ,bioUid
	    ,bioType);
	stepCt := stepCt + 1;
	msgTxt := 'Species added to bio_data_uid';
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,msgTxt,1,stepCt,'Done');
	rtnCd := 1;
    end if;

    return rtnCd;

exception
    when others then
	errorNumber := SQLSTATE;
	errorMessage := SQLERRM;
    --Handle errors.
	perform tm_cz.cz_error_handler (jobId, procedureName, errorNumber, errorMessage);
    --End Proc
	perform tm_cz.cz_end_audit (jobId, 'FAIL');
	return -16;

end;

$$;

