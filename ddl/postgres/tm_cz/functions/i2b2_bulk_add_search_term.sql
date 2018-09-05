--
-- Name: i2b2_bulk_add_search_term(bigint); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION i2b2_bulk_add_search_term(currentjobid bigint DEFAULT NULL::bigint) RETURNS void
    LANGUAGE plpgsql
    AS $_$
DECLARE

/*************************************************************************
* Copyright 2008-2012 Janssen Research null, LLC.
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
	newJobFlag numeric(1);
	databaseName varchar(100);
	procedureName varchar(100);
	jobID bigint;
	stepCt bigint;
	rowCt integer;

	v_keyword_term	varchar(500);
  v_display_category varchar(500);
  v_data_category varchar(500);
  v_prefix varchar(20);
  v_unique_ID varchar(500);
  v_source_cd varchar(500);
  v_parent_term varchar(500);
  v_bio_data_id bigint;
  
	sqlText			varchar(2000);
	Parent_Id 		integer;
	new_Term_Id 	integer;
	keyword_id 		integer;
	Lcount 			integer; 
	Ncount 			integer;
  
	--v_category_display	character varying(200);

	keyArray tm_lz.lt_src_search_keyword[] = array(select row(keyword, display_category, data_category, UID_prefix, unique_id, source_cd, parent_term, bio_data_id) 
	  	from tm_lz.lt_src_search_keyword
    		where (keyword IS NOT NULL AND keyword::text <> ''));
	keySize integer;


BEGIN

	--Set Audit Parameters
	newJobFlag := 0; -- False (Default)
	jobID := currentJobID;

	PERFORM sys_context('USERENV', 'CURRENT_SCHEMA') INTO databaseName ;
	procedureName := 'I2B2_BULK_ADD_SEARCH_TERM';

	--Audit JOB Initialization
	--If Job ID does not exist, then this is a single procedure run and we need to create it
	IF(coalesce(jobID::text, '') = '' or jobID < 1)
	THEN
		newJobFlag := 1; -- True
		perform cz_start_audit (procedureName, databaseName, jobID);
	END IF;
    	
	--stepCt := 0;
	--rowCt := 0;
  
	--perform cz_write_audit(jobId,databaseName,procedureName,'Start Procedure',0,stepCt,'Done');
	--Stepct := Stepct + 1;	

	keySize = array_length(keyArray, 1);
	for i in 0 .. (keySize - 1)
	loop
		v_keyword_term := keyArray[i].keyword;
    		v_display_category := keyArray[i].display_category;
    		v_data_category := keyArray[i].data_category;
    		v_prefix := keyArray[i].UID_prefix;
    		v_unique_ID := keyArray[i].unique_id;
    		v_source_cd := keyArray[i].source_cd;
    		v_parent_term := keyArray[i].parent_term;
    		v_bio_data_id := keyArray[i].bio_data_id;
    
    --dbms_output.put_line('keyword: ' || v_keyword_term);


		if (coalesce(v_display_category::text, '') = '') then
			v_display_category := v_data_category;
    		end if;


    		if (coalesce(v_unique_ID::text, '') = '') then
      		   if ((v_prefix IS NOT NULL AND v_prefix::text <> '')) then
        	      v_unique_ID := v_prefix || ':' || v_data_category || ':' || v_keyword_term;
      		   end if;
    		end if;
   

		-- Insert taxonomy term into searchapp.search_keyword
		-- (searches search_keyword with the parent term to find the category to use)
		insert into searchapp.search_keyword 
		(data_category
		,keyword
		,unique_id
		,source_code
		,display_data_category
    		,bio_data_id)
		select v_data_category
			  ,v_keyword_term
			  ,v_unique_ID
			  ,v_source_cd
			  ,v_display_category
        		  ,v_bio_data_id
		
		where not exists
			(select 1 from searchapp.search_keyword x
			 where upper(x.data_category) = upper(v_data_category)
			   and upper(x.keyword) = upper(v_keyword_term)
         		   and upper(x.bio_data_id) = upper(v_bio_data_id));
		get diagnostics rowCt := ROW_COUNT;	
		perform cz_write_audit(Jobid,Databasename,Procedurename,v_keyword_term || ' added to searchapp.search_keyword',rowCt,Stepct,'Done');
		Stepct := Stepct + 1;	
		commit;
  
		-- Get the ID of the new term in search_keyword
		select search_keyword_id  into keyword_id 
		from  searchapp.search_keyword 
		where upper(keyword) = upper(v_keyword_term)
		and upper(data_category) = upper(v_data_category)
    		and upper(bio_data_id) = upper(v_bio_data_id);
		get diagnostics rowCt := ROW_COUNT;
		perform cz_write_audit(Jobid,Databasename,Procedurename,'New search keyword ID stored in keyword_id',rowCt,Stepct,'Done');
		Stepct := Stepct + 1;	
		
		-- Insert the new term into searchapp.search_keyword_term 
		insert into searchapp.search_keyword_term 
		(keyword_term
		,search_keyword_id
		,rank
		,term_length)
		select upper(v_keyword_term)
			  ,keyword_id
			  ,1
			  ,length(v_keyword_term) 
		
		where not exists
			(select 1 from searchapp.search_keyword_term x
			 where upper(x.keyword_term) = upper(v_keyword_term)
			   and x.search_keyword_id = keyword_id);
		get diagnostics rowCt := ROW_COUNT;
		perform cz_write_audit(Jobid,Databasename,Procedurename,'Term added to searchapp.search_keyword_term',rowCt,Stepct,'Done');
		Stepct := Stepct + 1;	
		commit;

	end loop;

	perform cz_write_audit(Jobid,Databasename,Procedurename,'End '|| procedureName,0,Stepct,'Done');
	Stepct := Stepct + 1;  

 
     ---Cleanup OVERALL JOB if this proc is being run standalone    
  IF newJobFlag = 1
  THEN
    perform cz_end_audit (jobID, 'SUCCESS');
  END IF;
  
    EXCEPTION
  WHEN OTHERS THEN
    --Handle errors.
    perform cz_error_handler(jobId, procedureName, SQLSTATE, SQLERRM);
    --End Proc
    perform cz_end_audit (jobID, 'FAIL');
  
END;
 
$_$;

