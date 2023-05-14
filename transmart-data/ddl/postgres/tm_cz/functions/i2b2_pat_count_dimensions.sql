--
-- Name: i2b2_pat_count_dimensions(character varying, character varying, character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_pat_count_dimensions(p_metadataTable character varying, p_schemaName character varying, p_observationTable character varying, p_facttablecolumn character varying, p_tablename character varying, p_columnname character varying, path character varying, currentjobid numeric DEFAULT 0) RETURNS void
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
    jobID 			numeric(18,0);
    stepCt 			numeric(18,0);
    thisRowCt			numeric(18,0);
    rowCt			numeric(18,0);
    errorNumber		character varying;
    errorMessage	character varying;
    tExplain 		text;

    v_sqlstr text;
    -- using cursor defined withing FOR RECORD IN QUERY loop below.
    curRecord RECORD;
    v_num integer;

    pathEscaped		character varying;
    topPath		character varying;
    metadataTable	character varying;
    schemaName		character varying;
    observationTable	character varying;
    facttableColumn	character varying;
    tableName		character varying;
    columnName		character varying;
BEGIN


    ----------------------------------------------------------------------------------------------------------------------
    -- i2b2_pat_count_dimensions(metadataTable, schemaName, observationTable, facttablecolumn, tablename, columnname, path, jobid)
    ----------------------------------------------------------------------------------------------------------------------

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_pat_count_dimensions';

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

    metadataTable    := lower(p_metadatatable);
    schemaName 	     := lower(p_schemaname);
    observationTable := lower(p_observationtable);
    facttableColumn  := lower(p_facttablecolumn);
    tableName        := lower(p_tablename);
    columnName       := lower(p_columnname);

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Selecting '||path||' concepts for column '||columnname||' as '|| facttablecolumn|| ' from i2b2metadata.'||metadataTable||' for '||schemaName||'.'||observationTable,0,stepCt,'Done');

    -- truncate the tables used in this function
    execute ('truncate table tm_cz.temp_dim_count_ont');
    execute ('truncate table tm_cz.temp_dim_ont_with_folders');
    execute ('truncate table tm_cz.temp_path_to_num');
    execute ('truncate table tm_cz.temp_concept_path');
    execute ('truncate table tm_cz.temp_path_counts');
    execute ('truncate table tm_cz.temp_final_counts_by_concept');

    v_sqlstr := 'insert into tm_cz.temp_dim_count_ont (' ||
	' c_fullname' ||
	' ,c_basecode' ||
	' ,c_hlevel)' ||
	' select c_fullname, c_basecode, c_hlevel ' ||
	' from i2b2metadata.' || metadataTable ||
	' where c_fullname like ''' || pathEscaped || '%'' escape ''`''' ||
	' and lower(c_facttablecolumn) = ''' || facttableColumn || ''' ' ||
	' and lower(c_columnname) = ''' || columnName || ''' ' ||
	' and lower(c_synonym_cd) = ''n'' ' ||
        ' and lower(c_columndatatype) = ''t'' ' ||
        ' and lower(c_operator) = ''like'' ' ||
        ' and m_applied_path = ''@'' ' ||
	' and coalesce(c_fullname, '''') <> '''' ' ||
	' and (c_visualattributes not like ''L%'' or c_basecode in (select distinct concept_cd from ' || schemaName || '.' || observationTable || ')) ';

--    raise notice 'execute %', v_sqlstr;
    execute v_sqlstr;
    get diagnostics rowCt := ROW_COUNT;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert into temp_dim_count_ont',rowCt,stepCt,'Done');

    for curRecord IN 
	select c_fullname,c_table_name from i2b2metadata.table_access 
	LOOP 
	if metadataTable = lower(curRecord.c_table_name) then
	    v_sqlstr := 'with recursive concepts (c_fullname, c_hlevel, c_basecode) as ('
		|| 'select c_fullname, c_hlevel, c_basecode '
		|| 'from tm_cz.temp_dim_count_ont '
		|| 'where c_fullname like ''' || replace(curRecord.c_fullname,'\','\\') || '%'' '
		|| ' and c_fullname like ''' || pathEscaped || '%'' escape ''`'''
		|| 'union all ' 
		|| 'select cast( '
		|| ' 	left(c_fullname, length(c_fullname)-position(''\'' in right(reverse(c_fullname), length(c_fullname)-1))) '
		|| '	   	as varchar(700) '
		|| '	) c_fullname, ' 
		|| 'c_hlevel-1 c_hlevel, c_basecode '
		|| 'from concepts '
		|| 'where concepts.c_hlevel>0 '
		|| ') '
		|| 'insert into tm_cz.temp_dim_ont_with_folders '
		|| 'select distinct c_fullname, c_basecode '
		|| ' from concepts '
		|| ' where c_fullname like ''' || replace(curRecord.c_fullname,'\','\\') || '%'' '
		|| ' and c_fullname like ''' || pathEscaped || '%'' escape ''`'''
		|| ' order by c_fullname, c_basecode ';
--	    raise notice 'execute %', v_sqlstr;
	    execute v_sqlstr;
	    get diagnostics rowCt := ROW_COUNT;
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert into temp_dim_ont_with_folders',rowCt,stepCt,'Done');

	end if;
    END LOOP;

    select count(*) from tm_cz.temp_dim_ont_with_folders into rowCt;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Table temp_dim_ont_with_folders rows:',rowCt,stepCt,'Done');
    
    select count(distinct c_fullname) from tm_cz.temp_dim_ont_with_folders into rowCt;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Table temp_dim_ont_with_folders c_fullname:',rowCt,stepCt,'Done');
    
    -- Assign a number to each path and use this for the join to the fact table!
    insert into tm_cz.temp_path_to_num
    select c_fullname, row_number() over (order by c_fullname) path_num
        from (
            select distinct c_fullname c_fullname
            from tm_cz.temp_dim_ont_with_folders
             where c_fullname is not null
	       and c_fullname<>''
        ) t;
    get diagnostics rowCt := ROW_COUNT;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert into temp_path_to_num',rowCt,stepCt,'Done');
		
    insert into tm_cz.temp_concept_path
    select path_num,c_basecode
      from tm_cz.temp_path_to_num n
	       inner join tm_cz.temp_dim_ont_with_folders o
		       on o.c_fullname=n.c_fullname
     where o.c_fullname is not null
       and c_basecode is not null;
    get diagnostics rowCt := ROW_COUNT;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert into temp_concept_path',rowCt,stepCt,'Done');
    
    v_sqlstr := 'insert into tm_cz.temp_path_counts '
	|| 'select p1.path_num, count(distinct patient_num) as num_patients '
	|| 'from tm_cz.temp_concept_path p1 '
	|| 'left join ' || schemaName || '.' || observationTable || ' o '
	|| 'on p1.c_basecode = o.concept_cd '
	|| 'and coalesce(p1.c_basecode, '''') <> ''''  '
	|| 'group by p1.path_num';

--    raise notice 'execute %', v_sqlstr;
    execute v_sqlstr;
    get diagnostics rowCt := ROW_COUNT;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Inserted into temp_path_counts',rowCt,stepCt,'Done');

    insert into tm_cz.temp_final_counts_by_concept
    select p.c_fullname, c.num_patients num_patients 
      from tm_cz.temp_path_counts c
               inner join tm_cz.temp_path_to_num p
		       on p.path_num=c.path_num
     order by p.c_fullname;
    get diagnostics rowCt := ROW_COUNT;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Inserted into temp_final_counts_by_concept',rowCt,stepCt,'Done');

--    v_sqlstr := ' update i2b2metadata.' || metadataTable || ' a set c_totalnum=b.num_patients '
--             || ' from tm_cz.temp_final_counts_by_concept b '
--             || ' where a.c_fullname=b.c_fullname '
--             || ' and lower(a.c_facttablecolumn)= ''' || facttableColumn || ''' '
--	     || ' and lower(a.c_tablename) = ''' || tableName || ''' '
--	     || ' and lower(a.c_columnname) = ''' || columnName || ''' ';

--    select count(*) into v_num
--      from tm_cz.temp_final_counts_by_concept
--     where num_patients is not null
--       and num_patients <> 0;

--    execute v_sqlstr;
--    stepCt := stepCt + 1;
--    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Updated c_totalnum in '||metadataTable,v_num,stepCt,'Done');
   
    insert into i2b2metadata.tm_concept_counts(concept_path,parent_concept_path, patient_count)
    select c_fullname
	   ,ltrim(SUBSTR(c_fullname, 1,tm_cz.instr(c_fullname, '\',-1,2)))
	   , num_patients
      from tm_cz.temp_final_counts_by_concept
     where num_patients>0;
    get diagnostics rowCt := ROW_COUNT;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Updated I2B2METADATA tm_concept_counts',rowCt,stepCt,'Done');

END; 
$BODY$
 VOLATILE
  COST 100;
