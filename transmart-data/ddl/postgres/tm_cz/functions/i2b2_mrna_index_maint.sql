--
-- Name: i2b2_mrna_index_maint(text, text, bigint); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_mrna_index_maint(run_type text DEFAULT 'DROP'::text, tablespace_name text DEFAULT 'INDX'::text, currentjobid bigint DEFAULT NULL::bigint) RETURNS void
    LANGUAGE plpgsql
AS $$

   -- Attention:
   -- Originally Oracle procedure
   -- need to check table names for patitions on postgresql
   -- tablename__nn
   -- beware of tablename_new or tablename_bkp only allow numbers

    declare

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

    runType	varchar(100);
    idxExists	bigint;
    pExists	bigint;
    localVar	varchar(20);
    bitmapVar	varchar(20);
    bitmapCompress	varchar(20);
    tableSpace	varchar(50);

    --Audit variables
    newJobFlag numeric(1);
    databaseName varchar(100);
    procedureName varchar(100);
    jobID integer;
    stepCt integer;
    rowCt integer;

begin

    runType := upper(run_type);
    tableSpace := upper(tablespace_name);

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_mrna_index_maint';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(coalesce(jobID::text, '') = '' or jobID < 1) then
	newJobFlag := 1; -- True
	perform tm_cz.cz_start_audit (procedureName, databaseName, jobID);
    end if;

    stepCt := 0;

    --	Determine if de_subject_microarray_data is partitioned, if yes, set localVar to local
    select count(*)
      into pExists
      from pg_tables
     where tablename = 'de_subject_microarray_data'
       and partitioned = 'YES';

    if pExists = 0 then
	localVar := null;
	bitmapVar := null;
	bitmapCompress := 'compress';
    else
	localVar := 'local';
	bitmapVar := 'bitmap';
	bitmapCompress := null;
    end if;

    if runType = 'DROP' then
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Start de_subject_microarray_data index drop',0,stepCt,'Done');
	--	drop the indexes
	select count(*)
	  into idxExists
	  from pg_indexes
	 where tablename = 'de_subject_microarray_data'
	   and indexname = 'de_microarray_data_idx1'
	   and owner = 'deapp';

	if idxexists = 1 then
	    execute('drop index deapp.de_microarray_data_idx1');
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Drop de_microarray_data_idx1',0,stepCt,'Done');
	end if;

	select count(*)
	  into idxExists
	  from pg_indexes
	 where tablename = 'de_subject_microarray_data'
	   and indexname = 'de_microarray_data_idx2'
	   and owner = 'deapp';

	if idxExists = 1 then
	    execute('drop index deapp.de_microarray_data_idx2');
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Drop de_microarray_data_idx2',0,stepCt,'Done');
	end if;

	select count(*)
	  into idxExists
	  from pg_indexes
	 where tablename = 'de_subject_microarray_data'
	   and indexname = 'de_microarray_data_idx3'
	   and owner = 'deapp';

	if idxExists = 1 then
	    execute('drop index deapp.de_microarray_data_idx3');
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Drop de_microarray_data_idx3',0,stepCt,'Done');
	end if;

	select count(*)
	  into idxExists
	  from pg_indexes
	 where tablename = 'de_subject_microarray_data'
	   and indexname = 'de_microarray_data_idx4'
	   and owner = 'deapp';

	if idxExists = 1 then
	    execute('drop index deapp.de_microarray_data_idx4');
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Drop de_microarray_data_idx4',0,stepCt,'Done');
	end if;

	select count(*)
	  into idxExists
	  from pg_indexes
	 where tablename = 'de_subject_microarray_data'
	   and indexname = 'de_microarray_data_idx5'
	   and owner = 'deapp';

	if idxExists = 1 then
	    EXECUTE('drop index deapp.de_microarray_data_idx5');
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Drop de_microarray_data_idx5',0,stepCt,'Done');
	end if;

	select count(*)
	  into idxExists
	  from pg_indexes
	 where tablename = 'de_subject_microarray_data'
	   and indexname = 'de_microarray_data_idx10'
	   and owner = 'deapp';

	if idxExists = 1 then
	    execute('drop index deapp.de_microarray_data_idx10');
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Drop de_microarray_data_idx10',0,stepCt,'Done');
	end if;

    else
	--	add indexes
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Start de_subject_microarray_data index create',0,stepCt,'Done');

	select count(*)
	  into idxExists
	  from pg_indexes
	 where tablename = 'de_subject_microarray_data'
	   and indexname = 'de_microarray_data_idx1'
	   and owner = 'deapp';

	if idxExists = 0 then
	    execute('create index deapp.de_microarray_data_idx1 on deapp.de_subject_microarray_data(trial_name, assay_id, probeset_id) ' || localVar || ' nologging compress tablespace "' || tableSpace || '"');
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create de_microarray_data_idx1',0,stepCt,'Done');
	end if;

	select count(*)
	  into idxExists
	  from pg_indexes
	 where tablename = 'de_subject_microarray_data'
	   and indexname = 'de_microarray_data_idx2'
	   and owner = 'deapp';

	if idxExists = 0 then
	    execute('create index deapp.de_microarray_data_idx2 on deapp.de_subject_microarray_data(assay_id, probeset_id) ' || localVar || ' nologging compress tablespace "' || tableSpace || '"');
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create de_microarray_data_idx2',0,stepCt,'Done');
	end if;

	select count(*)
	  into idxExists
	  from pg_indexes
	 where tablename = 'de_subject_microarray_data'
	   and indexname = 'de_microarray_data_idx3'
	   and owner = 'deapp';

	if idxExists = 0 then
	    execute('create ' || bitmapVar || ' index deapp.de_microarray_data_idx3 on deapp.de_subject_microarray_data(assay_id) ' || localVar || ' nologging ' || bitmapCompress || ' tablespace "' || tableSpace || '"');
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create de_microarray_data_idx3',0,stepCt,'Done');
	end if;

	select count(*)
	  into idxExists
	  from pg_indexes
	 where tablename = 'de_subject_microarray_data'
	   and indexname = 'de_microarray_data_idx4'
	   and owner = 'deapp';

	if idxExists = 0 then
	    execute('create ' || bitmapVar || ' index deapp.de_microarray_data_idx4 on deapp.de_subject_microarray_data(probeset_id) ' || localVar || ' nologging ' || bitmapCompress || ' tablespace "' || tableSpace || '"');
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create de_microarray_data_idx4',0,stepCt,'Done');
	end if;

	if pExists = 0 then
	    --	only create this index if the table is not partitioned.  This is the column that the table would be partitioned on

	    select count(*)
	    into idxExists
	    from pg_indexes
	    where tablename = 'de_subject_microarray_data'
	    and indexname = 'de_microarray_data_idx5'
	    and owner = 'deapp';

	    if idxExists = 0 then
		execute('create index deapp.de_microarray_data_idx5 on deapp.de_subject_microarray_data(trial_source) ' || localVar || ' nologging ' || bitmapCompress || ' tablespace "' || tableSpace || '"');
		stepCt := stepCt + 1;
		perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create de_microarray_data_idx5',0,stepCt,'Done');
	    end if;
	end if;

	/*		not used

			select count(*)
			into idxExists
			from apg_indexes
			where tablename = 'de_subject_microarray_data'
			and indexname = 'de_microarray_data_idx10'
			and owner = 'deapp';

			if idxexists = 0 then
			execute immediate('create index deapp.de_microarray_data_idx10 on deapp.de_subject_microarray_data(assay_id, subject_id, probeset_id, zscore) ' || localvar || ' nologging compress tablespace "' || tablespace || '"');
			stepct := stepct + 1;
			perform tm_cz.cz_write_audit(jobid,databasename,procedurename,'create de_microarray_data_idx10',0,stepct,'done');
			end if;
	 */

    end if;

    stepct := stepct + 1;
    get diagnostics rowct := row_count;
    perform tm_cz.cz_write_audit(jobid,databasename,procedurename,'end function'||procedurename,rowct,stepct,'done');
    commit;


    ---cleanup overall job if this proc is being run standalone
    if newjobflag = 1 then
	perform tm_cz.cz_end_audit (jobID, 'SUCCESS');
    end if;

exception
    when others then
    --Handle errors.
	perform tm_cz.cz_error_handler(jobId, procedureName, SQLSTATE, SQLERRM);

    --End Proc
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
end;

$$;

