--
-- Name: add_search_keyword(character varying, character varying, character varying, character varying, character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.add_search_keyword(keyWrd character varying, keyUid character varying, keyCat character varying, keySrc character varying, keyDisplay character varying, dataId integer) RETURNS integer
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

    newJobFlag		integer;
    databaseName	VARCHAR(100);
    procedureName	VARCHAR(100);
    jobId 		numeric(18,0);
    stepCt 		numeric(18,0);
    rowCt		numeric(18,0);
    errorNumber		character varying;
    errorMessage	character varying;

    keyId               integer;

begin

    stepCt := 0;

    --Set Audit Parameters

    databaseName := 'tm_cz';
    procedureName := 'add_search_keyword';

    select tm_cz.cz_start_audit (procedureName, databaseName) into jobId;

    select search_keyword_id from searchapp.search_keyword where keyword = keyWrd and unique_id = keyUid and data_category = keyCat into keyId;
    stepCt := stepCt + 1;
    if keyId > 0 then
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Keyword found in search_keyword',keyId,stepCt,'Done');
    else
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Adding keyword to search_keyword',0,stepCt,'Done');
	insert into searchapp.search_keyword (
            keyword
	    ,bio_data_id
	    ,unique_id
	    ,data_category
	    ,source_code
	    ,display_data_category)
	values(
	    keyWrd
	    ,dataId
	    ,keyUid
	    ,keyCat
	    ,keySrc
	    ,keyDisplay
	);
	select search_keyword_id from searchapp.search_keyword where keyword = keyWrd and unique_id = keyUid and data_category = 'SPECIES' into keyId;
	stepCt := stepCt + 1;
	msgTxt := 'Keyword added to search_keyword: ' || keyId;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,msgTxt,1,stepCt,'Done');
    end if;

    perform tm_cz.add_search_keyword_term(keyWrd, keyId);

    return keyId;

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

