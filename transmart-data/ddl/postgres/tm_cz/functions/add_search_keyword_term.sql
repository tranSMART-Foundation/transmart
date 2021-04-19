--
-- Name: add_search_keyword_term(character varying, numeric, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.add_search_keyword_term(keyTerm character varying,  keyId integer, synonym integer default 0) RETURNS integer
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

    databaseName	VARCHAR(100);
    procedureName	VARCHAR(100);
    jobId 		numeric(18,0);
    stepCt 		numeric(18,0);
    rowCt		numeric(18,0);
    errorNumber		character varying;
    errorMessage	character varying;

    trmId               integer;
    trmKey		character varying;
    trmRank		integer;
    lenKey		integer;

begin

    stepCt := 0;

    --Set Audit Parameters

    databaseName := 'tm_cz';
    procedureName := 'add_search_keyword_term';

    select tm_cz.cz_start_audit (procedureName, databaseName) into jobId;

    trmKey := upper(keyTerm);
    lenKey := length(trmKey);

    if(synonym) then
	-- Need to check keyid and term
	select search_keyword_term_id from searchapp.search_keyword_term where search_keyword_id = keyId and term = trmKey into trmId;
	if(trmId > 0) then
	    return trmId;
	else
	    trmRank := 2;
	end if;
    else
	-- primary, only check keyid with rank 1
	select search_keyword_term_id from searchapp.search_keyword_term where search_keyword_id = keyId and rank = 1 into trmId;
	if(trmId > 0) then
	    return trmId;
	else
	    trmRank := 1;
	end if;
    end if;

    -- here if the term does not exist

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Adding term to search_keyword_term',0,stepCt,'Done');
    insert into searchapp.search_keyword_term (
	keyword_term
	,search_keyword_id
	,rank
	,term_length)
    values(
	trmKey
	,keyId
	,trmRank
	,lenKey
    );
    select search_keyword_term_id from searchapp.search_keyword_term where search_keyword_id = keyId and rank = 1 into trmId;

    return trmId;

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
