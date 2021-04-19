--
-- Name: upgrade_mrna_data(bigint); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.upgrade_mrna_data(currentjobid bigint DEFAULT 0::bigint) RETURNS void
    LANGUAGE plpgsql
AS $$
    declare

    -- Attention: Rewrite needed for partitions underneath by name new_nn
    -- Oracle procedure
    -- Also to cater for postgres10+ partitioning

    --Audit variables
    newJobFlag 	numeric(1);
    databaseName 	varchar(100);
    procedureName varchar(100);
    jobID 		integer;
    stepCt 		integer;
    rowCt           integer;

    gexStudy	varchar(200);
    gexSource	varchar(200);
    pExists		integer;
    tText		varchar(2000);

    gexCt integer;
    gexSize integer;
    gex_study_array deapp.de_subject_sample_mapping[] = array(
	select row (trial_name, source_cd)
	  from (select distinct trial_name
				,coalesce(source_cd,'STD') as source_cd
		  from deapp.de_subject_sample_mapping
		 where platform = 'MRNA_AFFYMETRIX'
		 order by trial_name
		  	  ,source_cd) AS dssm);

    -- JEA@20120602	New


begin
    gexSize = array_length(gex_study_array,1);
    stepCt := 0;

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'upgrade_mrna_data';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(coalesce(jobID::text, '') = '' or jobID < 1) then
	newJobFlag := 1; -- True
	perform tm_cz.cz_start_audit (procedureName, databaseName, jobID);
    end if;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Start upgrade_mrna_data',0,stepCt,'Done');
    commit;

    --	get trial_names for all gex data



    stepCt := stepCt + 1; get diagnostics rowCt := ROW_COUNT;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Bulk Collect trial_names',rowCt,stepCt,'Done');
    gexCt := 0;
    for i in 0 .. (gexSize - 1) loop
	gexStudy := gex_study_array[i].trial_name;
	gexSource := gex_study_array[i].source_cd;

	--	check if new table is partitioned and if partition exists

	select count(*) into pExists
	  from pg_tables
	 where tablename = 'de_subject_microarray_data_new'
	   and partitioned = 'YES';

	if pExists > 0 then
	    select count(*) into pExists
	    from all_tab_partitions
	    where tablename = 'de_subject_microarray_data_new'
	    and partition_name = gexStudy || ':' || gexSource;

	    if pExists = 0 then
		tText := 'alter table deapp.de_subject_microarray_data_new add PARTITION "' || gexStudy || ':' || gexSource ||
		    '"  VALUES (' || '''' || gexStudy || ':' || gexSource || '''' || ') ' ||
		    'NOLOGGING COMPRESS TABLESPACE "TRANSMART" ';
		execute(tText);
		stepCt := stepCt + 1;
		perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Added ' || gexStudy || ':' || gexSource || ' partition to de_subject_microarray_data_new',0,stepCt,'Done');
	    end if;
	end if;

	insert into deapp.de_subject_microarray_data_new
		    (trial_source
		    ,trial_name
		    ,probeset_id
		    ,assay_id
		    ,patient_id
		    ,raw_intensity
		    ,log_intensity
		    ,zscore
		    )
	select sm.trial_name || ':' || coalesce(sm.source_cd,'STD')
	       ,sm.trial_name
	       ,sd.probeset_id
	       ,sm.assay_id
	       ,sm.patient_id
	       ,sd.raw_intensity
	       ,sd.log_intensity
	       ,sd.zscore
	  from deapp.de_subject_sample_mapping sm
	       ,deapp.de_subject_microarray_data sd
	 where sm.trial_name = gexStudy
	   and sm.source_cd = gexSource
	   and sm.platform = 'MRNA_AFFYMETRIX'
	   and sm.assay_id = sd.assay_id;

	stepCt := stepCt + 1; get diagnostics rowCt := ROW_COUNT;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Inserted ' || gexStudy || ':' || gexSource || ' to new table',rowCt,stepCt,'Done');

    end loop;

    --	drop indexes on de_subject_microarray_data

    perform tm_cz.i2b2_mrna_index_maint('DROP',null,jobId);

    --	rename existing de_subject_microarray_data to _old

    alter table deapp.de_subject_microarray_data rename to de_subject_microarray_data_old;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Rename old de_subject_microarray_data',0,stepCt,'Done');

    --	rename _new to de_subject_microarray_data

    alter table deapp.de_subject_microarray_data_new rename to de_subject_microarray_data;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Rename old de_subject_microarray_data',0,stepCt,'Done');

    --	add indexes to de_subject_microarray_data

    perform tm_cz.i2b2_mrna_index_maint('ADD',null,jobId);

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'End i2b2_audit',0,stepCt,'Done');

    commit;

    --Cleanup OVERALL JOB if this proc is being run standalone
    if newJobFlag = 1 hen
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

