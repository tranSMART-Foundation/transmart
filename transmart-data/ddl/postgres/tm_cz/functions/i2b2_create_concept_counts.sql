--
-- Name: i2b2_create_concept_counts(character varying, character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_create_concept_counts(trialid character varying, path character varying, currentjobid numeric DEFAULT 0) RETURNS numeric
    LANGUAGE plpgsql SECURITY DEFINER
AS $$
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
    rowCt			numeric(18,0);
    errorNumber		character varying;
    errorMessage	character varying;
    tExplain 		text;

    curRecord RECORD;
    v_sqlstring text = '';

begin

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_create_concept_counts';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(jobID IS NULL or jobID < 1) then
	newJobFlag := 1; -- True
	select tm_cz.cz_start_audit (procedureName, databaseName) into jobID;
    end if;

    stepCt := 0;

    raise NOTICE 'i2b2_create_concept_counts(%,%,0)', trialid, path;
    select count(*) from i2b2demodata.concept_counts
	 where concept_path like path || '%' escape '`' into rowCt;

    raise NOTICE 'Before delete: concept_counts % records below concept_path %', rowCt, path;

    begin
	delete from i2b2demodata.concept_counts
	 where concept_path like path || '%' escape '`';
	get diagnostics rowCt := ROW_COUNT;
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete counts for trial from I2B2DEMODATA concept_counts',rowCt,stepCt,'Done');

    select count(*) from i2b2demodata.concept_counts
	 where concept_path = path into rowCt;

    raise NOTICE 'After delete: concept_counts % records for concept_path %', rowCt, path;

    -- code based on i2b2metadata functions for populating the i2b2 totalnum table
    -- i2b2 temp tables substituted by temp_* tables in tm_cz

    -- clear the working tables

    execute ('truncate table tm_cz.temp_concept_path');
    execute ('truncate table tm_cz.temp_dim_count_ont');
    execute ('truncate table tm_cz.temp_dim_ont_with_folders');
    execute ('truncate table tm_cz.temp_final_counts_by_concept');
    execute ('truncate table tm_cz.temp_ont_pat_visit_dims');
    execute ('truncate table tm_cz.temp_path_counts');
    execute ('truncate table tm_cz.temp_path_to_num');
    

    raise info 'At %, running RunTotalnum()',clock_timestamp();
    v_startime := clock_timestamp();

    for curRecord IN 
        select distinct upper(c_table_name) as sqltext
        from i2b2metadata.table_access 
        where c_visualattributes like '%A%' 
    LOOP 
        raise info 'At %: Running: %',clock_timestamp(), curRecord.sqltext;

	IF tableName='@' OR tableName=curRecord.sqltext THEN
            v_sqlstring := 'select tm_cz.i2b2_pat_count_visits( ''' || curRecord.sqltext || ''' , ''i2b2demodata'', ''' || path || '''  )';
            execute v_sqlstring;
            v_duration := clock_timestamp()-v_startime;
            raise info '(BENCH) %,I2B2_PAT_COUNT_VISITS,%',curRecord,v_duration;
            v_startime := clock_timestamp();
            
            v_sqlstring := 'select tm_cz.i2b2_pat_count_dimensions( ''' || curRecord.sqltext || ''' , ''i2b2demodata'', ''observation_fact'',  ''concept_cd'', ''concept_dimension'', ''concept_path'', ''' || path || '''  )';
            execute v_sqlstring;
            v_duration :=  clock_timestamp()-v_startime;
            raise info '(BENCH) %,I2B2_PAT_COUNT_concept_dimension,%',curRecord,v_duration;
            v_startime := clock_timestamp();
            
            v_sqlstring := 'select tm_cz.i2b2_pat_count_dimensions( ''' || curRecord.sqltext || ''' , ''i2b2demodata'', ''observation_fact'' ,  ''provider_id'', ''provider_dimension'', ''provider_path'', ''' || path || '''  )';
            execute v_sqlstring;
            v_duration := clock_timestamp()-v_startime;
            raise info '(BENCH) %,I2B2_PAT_COUNT_provider_dimension,%',curRecord,v_duration;
            v_startime := clock_timestamp();
            
            v_sqlstring := 'select tm_cz.i2b2_pat_count_dimensions( ''' || curRecord.sqltext || ''' , ''i2b2demodata'', ''observation_fact'' ,  ''modifier_cd'', ''modifier_dimension'', ''modifier_path'', ''' || path || '''  )';
            execute v_sqlstring;
            v_duration := clock_timestamp()-v_startime;
            raise info '(BENCH) %,I2B2_PAT_COUNT_modifier_dimension,%',curRecord,v_duration;
            v_startime := clock_timestamp();

	    execute 'update i2b2metadata.table_access set c_totalnum=(select c_totalnum from ' || curRecord.sqltext || ' x where x.c_fullname=table_access.c_fullname)';
        END IF;

    END LOOP;

    -- count patient visits
    begin
	insert into tm_cz.temp_ont_pat_visit_dims (
	    c_fullname
	    ,c_basecode
	    ,c_facttablecolumn
	    ,c_tablename
	    ,c_columnname
	    ,c_operator
	    ,c_dimcode
	    numpats
	)
	select (
	    c_fullname
	    ,c_basecode
	    ,c_facttablecolumn
	    ,c_tablename
	    ,c_columnname
	    ,c_operator
	    ,c_dimcode
	    null::integer
	)
	  from i2b2metadata.i2b2
	 where m_applied_path = '@'
	   and c_fullname like path || '%'
	   and lower(c_tablename) in ('patient_dimension', 'visit_dimension');
	get diagnostics rowCt := ROW_COUNT;
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Load patient, visit dimension records',rowCt,stepCt,'Done');

    raise NOTICE 'Load % patient visit dimension records ', rowCt;

    for curRecord in
	select c_fullname, c_facttablecolumn, c_tablename, c_columnname, c_operator, c_dimcode
	from tm_cz.temp_ont_pat_visit_dims
	LOOP
	v_sqlstr = 'update tm_cz.temp_ont_pat_visit_dims '
	    || ' set numpats = ( '
	    || ' select count(distinct(patient_num)) '
	    || ' from i2b2metadata.' || curRecord.c_tablename
	    || ' where ' || curRecord.c_columnname || ' ' ;

    
    --	Join each node (folder or leaf) in the path to its leaf in the work table to count patient numbers

    begin
--    for tExplain in
--	EXPLAIN (ANALYZE, VERBOSE, BUFFERS)
	insert into i2b2demodata.concept_counts (
	    concept_path
	    ,parent_concept_path
	    ,patient_count)
	select fa.c_fullname
	       ,ltrim(SUBSTR(fa.c_fullname, 1,tm_cz.instr(fa.c_fullname, '\',-1,2)))
	       ,count(distinct ob.patient_num)
	  from i2b2metadata.i2b2 fa
	       ,i2b2metadata.i2b2 la
	left join i2b2demodata.observation_fact ob
	on la.c_basecode = ob.concept_cd
	 where fa.c_fullname like path || '%' escape '`'
	   and substr(fa.c_visualattributes,2,1) != 'H'
	   and la.c_fullname like fa.c_fullname || '%' escape '`'
	   and la.c_visualattributes like 'L%'
	group by fa.c_fullname
	,ltrim(SUBSTR(fa.c_fullname, 1,tm_cz.instr(fa.c_fullname, '\',-1,2)))
--	LOOP
--	    raise notice 'explain: %', tExplain;
--	END LOOP
           ;
	get diagnostics rowCt := ROW_COUNT;
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert counts for trial into I2B2DEMODATA concept_counts',rowCt,stepCt,'Done');

    raise NOTICE 'Insert % counts for trial into I2B2DEMODATA concept_counts', rowCt;

    --SET ANY NODE WITH MISSING OR ZERO COUNTS TO HIDDEN

    begin
--    for tExplain in
--    EXPLAIN (ANALYZE, VERBOSE, BUFFERS)
	update i2b2metadata.i2b2
	   set c_visualattributes = substr(c_visualattributes,1,1) || 'H' || substr(c_visualattributes,3,1)
	 where c_fullname like path || '%' escape '`'
	       and (not exists
		    (select 1 from i2b2demodata.concept_counts nc
		      where c_fullname = nc.concept_path)
		      or
		      exists
		      (select 1 from i2b2demodata.concept_counts zc
			where c_fullname = zc.concept_path
			  and zc.patient_count = 0)
	       )
	       and c_name != 'SECURITY'
--	LOOP
--	    raise notice 'explain: %', tExplain;
--	END LOOP
	;
	get diagnostics rowCt := ROW_COUNT;
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Nodes hidden with missing/zero counts for trial in I2B2DEMODATA concept_counts',rowCt,stepCt,'Done');

    raise NOTICE 'Hidden % nodes with missing/zero counts for trial in I2B2DEMODATA concept_counts', rowCt;

    -- clear the working tables

    execute ('truncate table tm_cz.temp_concept_path');
    execute ('truncate table tm_cz.temp_dim_count_ont');
    execute ('truncate table tm_cz.temp_dim_ont_with_folders');
    execute ('truncate table tm_cz.temp_final_counts_by_concept');
    execute ('truncate table tm_cz.temp_ont_pat_visit_dims');
    execute ('truncate table tm_cz.temp_path_counts');
    execute ('truncate table tm_cz.temp_path_to_num');
    
    ---Cleanup OVERALL JOB if this proc is being run standalone
    if newJobFlag = 1 then
	perform tm_cz.cz_end_audit (jobID, 'SUCCESS');
    end if;

    return 1;

end;

$$;

