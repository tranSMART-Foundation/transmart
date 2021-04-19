--
-- Name: add_concept_species(character varying, character varying, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.add_concept_species(conCode character varying, conCodeName character varying, conDesc character varying) RETURNS integer
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

    conId		integer;
    conUid		character varying(300);

begin

    stepCt := 0;

    --Set Audit Parameters

    select tm_cz.cz_start_audit (procedureName, databaseName) into jobId;

    databaseName := 'tm_cz';
    procedureName := 'add_concept_species';

    conUid := 'SPECIES:'::text || conCodeName;

    select bio_concept_code_id from biomart.bio_concept_code where code_type_name = 'SPECIES' and bio_concept_code = conCode into conId;
    stepCt := stepCt + 1;
    if conId > 0 then
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Species found in bio_concept_code',conId,stepCt,'Done');
    else
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Adding species to bio_concept_code',conId,stepCt,'Done');
	-- add to bio_concept_code
	insert into biomart.bio_concept_code (
	    bio_concept_code
	    ,code_name
	    ,code_description
	    ,code_type_name)
	values (
	    conCode
	    ,conCodeName
	    ,conDesc
	    ,'SPECIES'
	);
	select bio_concept_code_id from biomart.bio_concept_code where code_type_name = 'SPECIES' and bio_concept_code = conCode into conId;
	stepCt := stepCt + 1;
	msgTxt := 'species added to bio_concept_code with bio_concept_code_id ' || conId;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,msgTxt,1,stepCt,'Done');
    end if;

    perform tm_cz.add_bio_data_uid(conUid, 'BIO_CONCEPT_CODE', conId);

    perform tm_cz.add_search_keyword(conCode, conUid, 'SPECIES', NULL, 'Organism', conId);

    return conId;


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

