--
-- Name: i2b2_pat_count_visits(character varying, character varying, character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_pat_count_visits(p_tabname character varying, p_tableschema character varying, path character varying, currentjobid numeric DEFAULT 0) RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
AS $BODY$
    /*************************************************************************
     * Copyright 2021 Axiomedix Inc.
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
    jobID 		numeric(18,0);
    stepCt 		numeric(18,0);
    thisRowCt		numeric(18,0);
    rowCt		numeric(18,0);
    errorNumber		character varying;
    errorMessage	character varying;
    tExplain 		text;

    v_sqlstr		text;
    -- using cursor defined within FOR RECORD IN QUERY loop below.
    curRecord		RECORD;
    v_num		integer;
 
    pathEscaped		character varying;
    topPath		character varying;
    tabName		character varying;
    tableSchema		character varying;
BEGIN

    -----------------------------------------------------------------------------------------
    -- i2b2_pat_count_visits(tabname, tableschema, path, jobid)
    -----------------------------------------------------------------------------------------

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_pat_count_visits';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(jobID IS NULL or jobID < 1) then
	newJobFlag := 1; -- True
	select tm_cz.cz_start_audit (procedureName, databaseName) into jobID;
    end if;

    stepCt := 0;

    -- may need to replace \ to \\ and ' to '' in the path parameter

    topPath := path;
--    topPath := replace(replace(topPath,'\','\\'),'''','''''');
    pathEscaped := replace(topPath, '_', '`_');

    tabName     := lower(p_tabname);
    tableSchema := lower(p_tableschema);

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Starting visit counts',0,stepCt,'Done');

    -- truncate table used in this function
    execute ('truncate table tm_cz.temp_ont_pat_visit_dims');

    -- count patient visits
    -- for tranSMART, no data in i2b2metadata.i2b2 for c_tablename patient_dimension or visit_dimension
    begin
	v_sqlstr := 'insert into tm_cz.temp_ont_pat_visit_dims (' ||
	    ' c_fullname' ||
	    ' ,c_basecode' ||
	    ' ,c_facttablecolumn' ||
	    ' ,c_tablename' ||
	    ' ,c_columnname' ||
	    ' ,c_operator' ||
	    ' ,c_dimcode' ||
	    ' ,numpats' ||
	    ' )' ||
	    ' select ' ||
	    ' c_fullname' ||
	    ' ,c_basecode' ||
	    ' ,c_facttablecolumn' ||
	    ' ,c_tablename' ||
	    ' ,c_columnname' ||
	    ' ,c_operator' ||
	    ' ,c_dimcode' ||
	    ' ,null::integer AS numpats' ||
	    ' from i2b2metadata.' || tabname ||
	    ' where m_applied_path = ''@''' ||
	    ' and c_fullname like ''' || pathEscaped || '%'' escape ''`''' ||
	    ' and lower(c_tablename) in (''patient_dimension'', ''visit_dimension'' )';
--	raise notice 'execute %', v_sqlstr;
	execute(v_sqlstr);
	get diagnostics rowCt := ROW_COUNT;
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return;
    end;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Load patient, visit dimension records',rowCt,stepCt,'Done');

    rowCt := 0;

    -- rather than creating cursor and fetching rows into local variables, instead using record variable type to 
    -- access each element of the current row of the cursor
    For curRecord IN 
	select c_fullname, c_facttablecolumn, c_tablename, c_columnname, c_operator, c_dimcode from tm_cz.temp_ont_pat_visit_dims
	LOOP 
        -- simplified query to directly query distinct patient_num instead of querying list of patient_num to feed into outer query for the same
        -- result.  New style runs in approximately half the time as tested with all patients with a particular sex_cd value.
        -- Since c_facttablecolumn is ALWAYS populated with 'patient_num' for all rows accessed by this function the change to the function is 
        -- worthwhile.  Only in rare cases if changes to the ontology tables are made would the original query be needed, but only where 
        -- c_facttablecolumn would not be "patient_num AND the values saved in that column in the dimension table are shared between patients that 
        -- don't otherwise have the same ontology" would the original method return different results.  It is believed that those results would be 
        -- inaccurate since they would reflect the number of patients who have XXX like patients with this ontology rather than the number of patients
        -- with that ontology. 
        v_sqlstr := 'update tm_cz.temp_ont_pat_visit_dims '
            || ' set numpats =  ( '                     
            ||     ' select count(distinct(patient_num)) '
            ||     ' from ' || tableschema || '.' || curRecord.c_tablename 
            ||         ' where '|| curRecord.c_columnname || ' '  ;

	-- Complete with choice of tests for c_columnname

        CASE 
        WHEN lower(curRecord.c_columnname) = 'birth_date' 
            and lower(curRecord.c_tablename) = 'patient_dimension'
            and lower(curRecord.c_dimcode) like '%not recorded%' then 
            -- adding specific change of " WHERE patient_dimension.birth_date in ('not_recorded') " to " WHERE patient_dimension.birth_date IS NULL " 
            -- since IS NULL syntax is not supported in the ontology tables, but the birth_date column is a timestamp datatype and can be null, but cannot be
            -- the character string 'not recorded'
            v_sqlstr := v_sqlstr || ' is null';
        WHEN lower(curRecord.c_operator) = 'like' then 
            -- escaping escape characters and double quotes.  The extension of '\' to '\\' is needed in Postgres. Alternatively, a custom escape character
            -- could be listed in the query if it is known for certain that that character will never be found in any c_dimcode value accessed by this 
            -- function
            v_sqlstr := v_sqlstr || curRecord.c_operator  || ' ' || '''' || replace(replace(curRecord.c_dimcode,'\','\\'),'''','''''') || '%''' ;
        WHEN lower(curRecord.c_operator) = 'in' then 
            v_sqlstr := v_sqlstr || curRecord.c_operator  || ' ' ||  '(' || curRecord.c_dimcode || ')';
        WHEN lower(curRecord.c_operator) = '=' then 
            v_sqlstr := v_sqlstr || curRecord.c_operator  || ' ''' ||  replace(curRecord.c_dimcode,'''','''''') || '''';
        ELSE 
            v_sqlstr := v_sqlstr || curRecord.c_operator  || ' ' || curRecord.c_dimcode;
        END CASE;
            
        v_sqlstr := v_sqlstr ||
            ' ) ' ||
            ' where c_fullname = ' || '''' || curRecord.c_fullname || '''' ||
            ' and numpats is null';
    
	begin
--	    raise notice 'execute %', v_sqlstr;
            execute v_sqlstr;
	    get diagnostics thisRowCt := ROW_COUNT;
	    rowCt := rowCt + thisRowCt;
	EXCEPTION
	    WHEN OTHERS THEN
		errorNumber := SQLSTATE;
		errorMessage := SQLERRM;
	    --Handle errors.
		perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	    --End Proc
		perform tm_cz.cz_end_audit (jobID, 'FAIL');
		return;
	    -- keep looping
   	END;
	--else
        -- do nothing since we do not have the column in our schema
	--   end if;
    END LOOP;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Updated patient, visit dimension records',rowCt,stepCt,'Done');

    v_sqlstr := 'update i2b2metadata.' || tabname || ' a set c_totalnum=b.numpats '
        || ' from tm_cz.temp_ont_pat_visit_dims b '
        || ' where a.c_fullname=b.c_fullname and b.numpats>0';

    --display count and timing information to the user
    select count(*) into v_num from tm_cz.temp_ont_pat_visit_dims where numpats is not null and numpats <> 0;
             
--    raise notice 'execute %', v_sqlstr;
    execute v_sqlstr;
    get diagnostics rowCt := ROW_COUNT;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Table ' || tabname ||' updated',rowCt,stepCt,'Done');

    insert into i2b2metadata.tm_concept_counts(
	concept_path
	,parent_concept_path
	,patient_count
    )
    select c_fullname
	   ,ltrim(SUBSTR(c_fullname, 1,tm_cz.instr(c_fullname, '\',-1,2)))
	   ,numpats
      from tm_cz.temp_ont_pat_visit_dims;
    get diagnostics rowCt := ROW_COUNT;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Updated I2B2METADATA tm_concept_counts',rowCt,stepCt,'Done');

    return;

END;
$BODY$
 VOLATILE;
  
 
