--
-- Name: i2b2_process_mrna_data(character varying, character varying, character varying, character varying, numeric, character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_process_mrna_data(trial_id character varying, top_node character varying, data_type character varying DEFAULT 'R'::character varying, source_cd character varying DEFAULT 'STD'::character varying, log_base numeric DEFAULT 2, secure_study character varying DEFAULT 'N'::character varying, currentjobid numeric DEFAULT 0) RETURNS numeric
    LANGUAGE plpgsql SECURITY DEFINER
AS $$

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
    Declare

    --Audit variables
    newJobFlag		integer;
    databaseName 	VARCHAR(100);
    procedureName 	VARCHAR(100);
    jobID 		numeric(18,0);
    stepCt 		numeric(18,0);
    rowCt		numeric(18,0);
    thisRowCt		numeric(18,0);
    errorNumber		character varying;
    errorMessage	character varying;

    TrialID		varchar(100);
    RootNode		varchar(2000);
    root_level		integer;
    topNode		varchar(2000);
    topLevel		integer;
    tPath		varchar(2000);
    study_name		varchar(100);
    sourceCd		varchar(50);
    secureStudy		varchar(1);

    dataType		varchar(10);
    sqlText		varchar(1000);
    tText		varchar(1000);
    gplTitle		varchar(1000);
    pExists		numeric;
    partTbl   		numeric;
    partExists 		numeric;
    sampleCt		numeric;
    idxExists 		numeric;
    logBase		numeric;
    pCount		integer;
    sCount		integer;
    tablespaceName	varchar(200);
    partitioniD		numeric(18,0);
    partitionName	varchar(100);
    partitionIndx	varchar(100);
    rtnCd		integer;

    --	cursor to add leaf nodes, cursor is used here because there are few nodes to be added

    addNodes CURSOR is
    select distinct t.leaf_node
		    ,t.node_name
      from  tm_wz.wt_mrna_nodes t
     where not exists
	   (select 1 from i2b2metadata.i2b2 x
	     where t.leaf_node = x.c_fullname);

    --	cursor to define the path for delete_one_node  this will delete any nodes that are hidden after i2b2_create_concept_counts

    delNodes CURSOR is
    select distinct c_fullname
      from  i2b2metadata.i2b2
     where c_fullname like topNode || '%' escape '`'
       and substr(c_visualattributes,2,1) = 'H';

BEGIN
    TrialID := upper(trial_id);
    secureStudy := upper(secure_study);

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;
    databaseName := 'tm_cz';
    procedureName := 'i2b2_process_mrna_data';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it

    if (jobID IS NULL or jobID < 1) then
	newJobFlag := 1; -- True
	select tm_cz.cz_start_audit (procedureName, databaseName) into jobID;
    end if;

    stepCt := 0;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Starting i2b2_process_mrna_data',0,stepCt,'Done');

    if (secureStudy not in ('Y','N') ) then
	secureStudy := 'Y';
    end if;

    topNode := REGEXP_REPLACE('\' || top_node || '\','(\\){2,}', '\','g');
    select length(topNode)-length(replace(topNode,'\','')) into topLevel;

    if data_type is null then
	dataType := 'R';
    else
	if data_type in ('R','T','L') then
	    dataType := data_type;
	else
	    dataType := 'R';
	end if;
    end if;

    logBase := coalesce(log_base, 2);
    sourceCd := upper(coalesce(source_cd,'STD'));

    --	Get count of records in tm_lz.lt_src_mrna_subj_samp_map

    select count(*) into sCount
      from tm_lz.lt_src_mrna_subj_samp_map;

    --	check if all subject_sample map records have a subject_id, If not, abort run

    select count(*) into pCount
      from tm_lz.lt_src_mrna_subj_samp_map t
     where subject_id is null;

    if pCount > 0 then
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'subject_id missing in lt_src_mrna_subj_samp_map',0,pCount,'Done');
	perform tm_cz.cz_error_handler (jobID, procedureName, '-1', 'Application raised error');
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
	return -16;
    end if;

    --	check if all subject_sample map records have a sample_cd, If not, abort run

    select count(*) into pCount
      from tm_lz.lt_src_mrna_subj_samp_map t
     where sample_cd is null;

    if pCount > 0 then
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'sample_cd missing in lt_src_mrna_subj_samp_map',0,pCount,'Done');
	perform tm_cz.cz_error_handler (jobID, procedureName, '-1', 'Application raised error');
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
	return -16;
    end if;

    --	check if all subject_sample map records have a platform, If not, abort run

    select count(*) into pCount
      from tm_lz.lt_src_mrna_subj_samp_map t
     where platform is null;

    if pCount > 0 then
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Platform missing in lt_src_mrna_subj_samp_map',0,pCount,'Done');
	perform tm_cz.cz_error_handler (jobID, procedureName, '-1', 'Application raised error');
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
	return -16;
    end if;

    --	check if platform exists in de_mrna_annotation .  If not, abort run.

    select count(*) into pCount
      from deapp.de_mrna_annotation
     where gpl_id in (select distinct m.platform from tm_lz.lt_src_mrna_subj_samp_map m);

    if pCount = 0 then
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'No Gene Expression platforms in deapp.de_mrna_annotation',0,pCount,'Done');
	perform tm_cz.cz_error_handler (jobID, procedureName, '-1', 'Application raised error');
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
	return -16;
    end if;

    --	check if entry in deapp.de_gpl_info for every Gene Expression platform, if not, abort run

    select count(*) into pCount
      from tm_lz.lt_src_mrna_subj_samp_map sm
     where not exists
	   (select 1 from deapp.de_gpl_info gi
	     where sm.platform = gi.platform
	       and gi.marker_type = 'Gene Expression'
	       and gi.title is not null);

    if pCount > 0 then
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'deapp.de_gpl_info entry missing for one or more platforms',0,pCount,'Done');
	perform tm_cz.cz_error_handler (jobID, procedureName, '-1', 'Application raised error');
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
	return -16;
    end if;

    --	check if all subject_sample map records have a tissue_type, If not, abort run

    select count(*) into pCount
      from tm_lz.lt_src_mrna_subj_samp_map
     where tissue_type is null;

    if pCount > 0 then
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Tissue_Type is null for subjects',0,pCount,'Done');
	perform tm_cz.cz_error_handler (jobID, procedureName, '-1', 'Application raised error');
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
	return -16;
    end if;

    --	check if there are multiple platforms for a single sample   if yes, then different source_cd must be used to load the samples.

    select count(*) into pCount
      from (select sample_cd
	      from tm_lz.lt_src_mrna_subj_samp_map
	     group by sample_cd
	    having count(distinct platform) > 1) x;

    if pCount > 0 then
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Multiple platforms for single sample',0,pCount,'Done');
	perform tm_cz.cz_error_handler (jobID, procedureName, '-1', 'Application raised error');
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
	return -16;
    end if;

    -- Get root_node from topNode

    select tm_cz.parse_nth_value(topNode, 2, '\') into RootNode;

    select count(*) into pExists
      from i2b2metadata.i2b2
     where c_name = rootNode;

    if pExists = 0 then
	perform tm_cz.i2b2_add_root_node(rootNode, jobId);
    end if;

    select c_hlevel into root_level
      from i2b2metadata.table_access
     where c_name = RootNode;

    -- Get study name from topNode

    select tm_cz.parse_nth_value(topNode, topLevel, '\') into study_name;

    --	Add any upper level nodes as needed

    tPath := REGEXP_REPLACE(replace(top_node,study_name,''),'(\\){2,}', '\', 'g');
    select length(tPath) - length(replace(tPath,'\','')) into pCount;

    if pCount > 2 then
	select tm_cz.i2b2_fill_in_tree('', tPath, jobId) into rtnCd;
	if(rtnCd <> 1) then
	    tText := 'Failed to fill in tree '|| tPath;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,0,stepCt,'Message');
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
        end if;
    end if;

    --	uppercase study_id in tm_lz.lt_src_mrna_subj_samp_map in case curator forgot

    begin
	update tm_lz.lt_src_mrna_subj_samp_map
	   set trial_name=upper(trial_name);
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Uppercase trial_name in tm_lz.lt_src_mrna_subj_samp_map',rowCt,stepCt,'Done');

    select tm_cz.load_tm_trial_nodes(TrialID,topNode,jobID,false) into rtnCd;

    if(rtnCd <> 1) then
       stepCt := stepCt + 1;
       perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Failed to load tm_trial_nodes',0,stepCt,'Message');
       perform tm_cz.cz_end_audit (jobID, 'FAIL');
       return -16;
    end if;

    --	create records in patient_dimension for subject_ids if they do not exist
    --	format of sourcesystem_cd:  trial:[site:]subject_cd

    begin
	insert into i2b2demodata.patient_dimension (
	    patient_num
	    ,sex_cd
	    ,age_in_years_num
	    ,race_cd
	    ,update_date
	    ,download_date
	    ,import_date
	    ,sourcesystem_cd
	)
	select nextval('i2b2demodata.seq_patient_num')
	       ,x.sex_cd
	       ,x.age_in_years_num
	       ,x.race_cd
	       ,current_timestamp
	       ,current_timestamp
	       ,current_timestamp
	       ,x.sourcesystem_cd
	  from (select distinct 'Unknown' as sex_cd
				,null::integer as age_in_years_num
				,null as race_cd
				,regexp_replace(TrialID || ':' || coalesce(s.site_id,'') || ':' || s.subject_id,'(::){1,}', ':', 'g') as sourcesystem_cd
		  from tm_lz.lt_src_mrna_subj_samp_map s
		       ,deapp.de_gpl_info g
		 where s.subject_id is not null
		   and s.trial_name = TrialID
		   and s.source_cd = sourceCD
		   and s.platform = g.platform
		   and upper(g.marker_type) = 'GENE EXPRESSION'
		   and not exists
		       (select 1 from i2b2demodata.patient_dimension x
			 where x.sourcesystem_cd =
			       regexp_replace(TrialID || ':' || coalesce(s.site_id,'') || ':' || s.subject_id,'(::){1,}', ':', 'g'))
	  ) x;
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert subjects to patient_dimension',rowCt,stepCt,'Done');

    --	add security for trial if new subjects added to patient_dimension

    if pCount > 0 then
	perform tm_cz.i2b2_create_security_for_trial(TrialId, secureStudy, jobID);
    end if;

    --	Delete existing observation_fact data, will be repopulated

    begin
	delete from i2b2demodata.observation_fact obf
	 where obf.concept_cd in
	       (select distinct x.concept_code
		  from deapp.de_subject_sample_mapping x
		 where x.trial_name = TrialId
		   and coalesce(x.source_cd,'STD') = sourceCD
		   and x.platform = 'MRNA_AFFYMETRIX');
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete data from observation_fact',rowCt,stepCt,'Done');

    --	get next partitionId
    select nextval('deapp.seq_mrna_partition_id') into partitionId;

    partitionName := 'deapp.de_subject_microarray_data_' || partitionId::text; -- revert to using partitions
    partitionIndx := 'de_subject_microarray_data_' || partitionId::text; -- revert to using partitions

    --	truncate tmp node table

    execute ('truncate table tm_wz.wt_mrna_nodes');

    --	load temp table with leaf node path, use temp table with distinct sample_type, ATTR2, platform, and title
    --	this was faster than doing subselect from wt_subject_mrna_data

    execute ('truncate table tm_wz.wt_mrna_node_values');

    begin
	insert into tm_wz.wt_mrna_node_values (
	    category_cd
	    ,platform
	    ,tissue_type
	    ,attribute_1
	    ,attribute_2
	    ,title
	)
	select distinct a.category_cd
			,coalesce(a.platform,'GPL570')
			,coalesce(a.tissue_type,'Unspecified Tissue Type')
			,a.attribute_1
			,a.attribute_2
			,g.title
	  from tm_lz.lt_src_mrna_subj_samp_map a
	       ,deapp.de_gpl_info g
	 where a.trial_name = TrialID
	   and coalesce(a.platform,'GPL570') = g.platform
	   and a.source_cd = sourceCD
	   and upper(g.marker_type) = 'GENE EXPRESSION';
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert node values into DEAPP tm_wz.wt_mrna_node_values',rowCt,stepCt,'Done');

    --	inserts that create the ontology for the leaf nodes

    begin
	insert into tm_wz.wt_mrna_nodes (
	    leaf_node
	    ,category_cd
	    ,platform
	    ,tissue_type
	    ,attribute_1
            ,attribute_2
	    ,node_type
	)
	select distinct topNode || regexp_replace(replace(replace(replace(replace(replace(replace(
	    category_cd,'PLATFORM',title),
	    'ATTR1',coalesce(attribute_1,'')),
	    'ATTR2',coalesce(attribute_2,'')),
	    'TISSUETYPE',coalesce(tissue_type,'')),
	    '+','\'),
	    '_',' ') || '\','(\\){2,}', '\', 'g')
	       		,category_cd
			,platform as platform
			,tissue_type
			,attribute_1 as attribute_1
			,attribute_2 as attribute_2
			,'LEAF'
	  from  tm_wz.wt_mrna_node_values;
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create leaf nodes in DEAPP tmp_mrna_nodes',rowCt,stepCt,'Done');

    --	insert for platform node so platform concept can be populated

    begin
	insert into tm_wz.wt_mrna_nodes (
	    leaf_node
	    ,category_cd
	    ,platform
	    ,tissue_type
	    ,attribute_1
            ,attribute_2
	    ,node_type
	)
	select distinct topNode || regexp_replace(replace(replace(replace(replace(replace(replace(
	    substr(category_cd,1,tm_cz.instr(category_cd,'PLATFORM')+8),
	    'PLATFORM',title),
	    'ATTR1',coalesce(attribute_1,'')),
	    'ATTR2',coalesce(attribute_2,'')),
	    'TISSUETYPE',coalesce(tissue_type,'')),
	    '+','\'),
	    '_',' ') || '\', '(\\){2,}', '\', 'g')
			,substr(category_cd,1,tm_cz.instr(category_cd,'PLATFORM')+8)
			,platform as platform
			,case when tm_cz.instr(substr(category_cd,1,tm_cz.instr(category_cd,'PLATFORM')+8),'TISSUETYPE') > 1 then tissue_type else '' end as tissue_type
			,case when tm_cz.instr(substr(category_cd,1,tm_cz.instr(category_cd,'PLATFORM')+8),'ATTR1') > 1 then attribute_1 else '' end as attribute_1
			,case when tm_cz.instr(substr(category_cd,1,tm_cz.instr(category_cd,'PLATFORM')+8),'ATTR2') > 1 then attribute_2 else '' end as attribute_2
			,'PLATFORM'
	  from  tm_wz.wt_mrna_node_values
	  where category_cd like '%PLATFORM%'
	   and platform is not null
	       and platform::text <> '';
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create platform nodes in tm_wz.wt_mrna_nodes',rowCt,stepCt,'Done');

    --	insert for ATTR1 node so ATTR1 concept can be populated in sample_type_cd

    begin
	insert into tm_wz.wt_mrna_nodes (
	    leaf_node
	    ,category_cd
	    ,platform
	    ,tissue_type
	    ,attribute_1
	    ,attribute_2
	    ,node_type
	)
	select distinct topNode || regexp_replace(replace(replace(replace(replace(replace(replace(
	    substr(category_cd,1,tm_cz.instr(category_cd,'ATTR1')+5),
	    'PLATFORM',title),
	    'ATTR1',coalesce(attribute_1,'')),
	    'ATTR2',coalesce(attribute_2,'')),
	    'TISSUETYPE',coalesce(tissue_type,'')),
	    '+','\'),
	    '_',' ') || '\',
	    '(\\){2,}', '\', 'g')
			,substr(category_cd,1,tm_cz.instr(category_cd,'ATTR1')+5)
			,case when tm_cz.instr(substr(category_cd,1,tm_cz.instr(category_cd,'ATTR1')+5),'PLATFORM') > 1 then platform else '' end as platform
			,case when tm_cz.instr(substr(category_cd,1,tm_cz.instr(category_cd,'ATTR1')+5),'TISSUETYPE') > 1 then tissue_type else '' end as tissue_type
			,attribute_1 as attribute_1
			,case when tm_cz.instr(substr(category_cd,1,tm_cz.instr(category_cd,'ATTR1')+5),'ATTR2') > 1 then attribute_2 else '' end as attribute_2
			,'ATTR1'
	  from  tm_wz.wt_mrna_node_values
	 where category_cd like '%ATTR1%'
	   and attribute_1 is not null
	       and attribute_1::text <> '';
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create ATTR1 nodes in tm_wz.wt_mrna_nodes',rowCt,stepCt,'Done');

    --	insert for ATTR2 node so ATTR2 concept can be populated in timepoint_cd

    begin
	insert into tm_wz.wt_mrna_nodes (
	    leaf_node
	    ,category_cd
	    ,platform
	    ,tissue_type
            ,attribute_1
	    ,attribute_2
	    ,node_type
	)
	select
	    distinct topNode || regexp_replace(replace(replace(replace(replace(replace(replace(
		substr(category_cd,1,tm_cz.instr(category_cd,'ATTR2')+5),
		'PLATFORM',title),
		'ATTR1',coalesce(attribute_1,'')),
		'ATTR2',coalesce(attribute_2,'')),
		'TISSUETYPE',coalesce(tissue_type,'')),
		'+','\'),
		'_',' ') || '\',
		'(\\){2,}', '\', 'g')
	    ,substr(category_cd,1,tm_cz.instr(category_cd,'ATTR2')+5)
	    ,case when tm_cz.instr(substr(category_cd,1,tm_cz.instr(category_cd,'ATTR2')+5),'PLATFORM') > 1 then platform else '' end as platform
	    ,case when tm_cz.instr(substr(category_cd,1,tm_cz.instr(category_cd,'ATTR2')+5),'TISSUETYPE') > 1 then tissue_type else '' end as tissue_type
	    ,case when tm_cz.instr(substr(category_cd,1,tm_cz.instr(category_cd,'ATTR2')+5),'ATTR1') > 1 then attribute_1 else '' end as attribute_1
	    ,attribute_2 as attribute_2
	    ,'ATTR2'
	  from  tm_wz.wt_mrna_node_values
	 where category_cd like '%ATTR2%'
	   and attribute_2 is not null
	   and attribute_2::text <> '';
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create ATTR2 nodes in tm_wz.wt_mrna_nodes',rowCt,stepCt,'Done');

    --	insert for tissue_type node so tissue_type_cd can be populated

    begin
	insert into tm_wz.wt_mrna_nodes (
	    leaf_node
	    ,category_cd
	    ,platform
	    ,tissue_type
	    ,attribute_1
            ,attribute_2
	    ,node_type
	)
	select distinct topNode || regexp_replace(replace(replace(replace(replace(replace(replace(
	    substr(category_cd,1,tm_cz.instr(category_cd,'TISSUETYPE')+10),
	    'PLATFORM',title),
	    'ATTR1',coalesce(attribute_1,'')),
	    'ATTR2',coalesce(attribute_2,'')),
	    'TISSUETYPE',coalesce(tissue_type,'')),
	    '+','\'),
	    '_',' ') || '\',
	    '(\\){2,}', '\', 'g')
			,substr(category_cd,1,tm_cz.instr(category_cd,'TISSUETYPE')+10)
			,case when tm_cz.instr(substr(category_cd,1,tm_cz.instr(category_cd,'TISSUETYPE')+10),'PLATFORM') > 1 then platform else '' end as platform
			,tissue_type as tissue_type
			,case when tm_cz.instr(substr(category_cd,1,tm_cz.instr(category_cd,'TISSUETYPE')+10),'ATTR1') > 1 then attribute_1 else '' end as attribute_1
			,case when tm_cz.instr(substr(category_cd,1,tm_cz.instr(category_cd,'TISSUETYPE')+10),'ATTR2') > 1 then attribute_2 else '' end as attribute_2
			,'TISSUETYPE'
	  from  tm_wz.wt_mrna_node_values
	 where category_cd like '%TISSUETYPE%'
	   and (tissue_type is NOT NULL
		and tissue_type::text <> '');
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create TISSUETYPE nodes in tm_wz.wt_mrna_nodes',rowCt,stepCt,'Done');

    --	set node_name

    begin
	update tm_wz.wt_mrna_nodes
	   set node_name=tm_cz.parse_nth_value(leaf_node,length(leaf_node)-length(replace(leaf_node,'\','')),'\');
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Updated node_name in DEAPP tmp_mrna_nodes',rowCt,stepCt,'Done');

    --	add leaf nodes for mRNA data  The cursor will only add nodes that do not already exist.

    FOR r_addNodes in addNodes Loop

	--Add nodes for all types (ALSO DELETES EXISTING NODE)

	select tm_cz.i2b2_add_node(TrialID, r_addNodes.leaf_node, r_addNodes.node_name, jobId) into rtnCd;
	stepCt := stepCt + 1;
	if(rtnCd <> 1) then
            tText := 'Failed to add leaf node '|| r_addNodes.leaf_node;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,0,stepCt,'Message');
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
        end if;
	tText := 'Added Leaf Node: ' || r_addNodes.leaf_node || '  Name: ' || r_addNodes.node_name;

	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,1,stepCt,'Done');

	select tm_cz.i2b2_fill_in_tree(TrialId, r_addNodes.leaf_node, jobID) into rtnCd;
	if(rtnCd <> 1) then
	    tText := 'Failed to fill in tree '|| r_addNodes.leaf_node;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,0,stepCt,'Message');
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
        end if;

    END LOOP;

    --	update concept_cd for nodes, this is done to make the next insert easier

    begin
	update tm_wz.wt_mrna_nodes t
	   set concept_cd=(
	       select c.concept_cd
		 from i2b2demodata.concept_dimension c
	        where c.concept_path = t.leaf_node
	   )
	 where exists
               (select 1 from i2b2demodata.concept_dimension x
	         where x.concept_path = t.leaf_node
	       )
	       and t.concept_cd is null;
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update tm_wz.wt_mrna_nodes with newly created concept_cds',rowCt,stepCt,'Done');

    --Update or insert DE_SUBJECT_SAMPLE_MAPPING from wt_subject_mrna_data

    --PATIENT_ID      = PATIENT_ID (SAME AS ID ON THE PATIENT_DIMENSION)
    --SITE_ID         = site_id
    --SUBJECT_ID      = subject_id
    --SUBJECT_TYPE    = NULL
    --CONCEPT_CODE    = from LEAF records in tm_wz.wt_mrna_nodes
    --SAMPLE_TYPE    	= attribute_1
    --SAMPLE_TYPE_CD  = concept_cd from ATTR1 records in tm_wz.wt_mrna_nodes
    --TRIAL_NAME      = TRIAL_NAME
    --TIMEPOINT		= attribute_2
    --TIMEPOINT_CD	= concept_cd from ATTR2 records in tm_wz.wt_mrna_nodes
    --TISSUE_TYPE     = TISSUE_TYPE
    --TISSUE_TYPE_CD  = concept_cd from TISSUETYPE records in tm_wz.wt_mrna_nodes
    --PLATFORM        = MRNA_AFFYMETRIX - this is required by ui code
    --PLATFORM_CD     = concept_cd from PLATFORM records in tm_wz.wt_mrna_nodes
    --DATA_UID		= concatenation of concept_cd-patient_num
    --GPL_ID			= platform from wt_subject_mrna_data
    --CATEGORY_CD		= category_cd that generated ontology
    --SAMPLE_ID		= id of sample (trial:S:[site_id]:subject_id:sample_cd) from patient_dimension, may be the same as patient_num
    --SAMPLE_CD		= sample_cd
    --SOURCE_CD		= sourceCd
    --PARTITION_ID	= partitionId

    --ASSAY_ID        = generated by trigger

    begin
	with upd as (select a.site_id
			    , a.subject_id
			    , a.sample_cd
			    , ln.concept_cd as concept_code
			    , ttp.concept_cd as tissue_type_cd
			    , a2.concept_cd as timepoint_cd
			    , a1.concept_cd as sample_type_cd
			    , a.category_cd
			    , pd.patient_num as patient_id
			    , ln.concept_cd || '-' || pd.patient_num::text as data_uid
			    , ln.tissue_type as tissue_type
			    , ln.attribute_1 as sample_type
			    , ln.attribute_2 as timepoint
			    , a.platform as gpl_id
		       from tm_lz.lt_src_mrna_subj_samp_map a
				inner join i2b2demodata.patient_dimension pd
					on regexp_replace(TrialID || ':' || coalesce(a.site_id,'') || ':' || a.subject_id,'(::){1,}', ':', 'g') = pd.sourcesystem_cd
				inner join tm_wz.wt_mrna_nodes ln
					on  a.platform = ln.platform
					and a.category_cd = ln.category_cd
					and coalesce(a.tissue_type,'@') = coalesce(ln.tissue_type,'@')
					and coalesce(a.attribute_1,'@') = coalesce(ln.attribute_1,'@')
					and coalesce(a.attribute_2,'@') = coalesce(ln.attribute_2,'@')
					and ln.node_type = 'LEAF'
				left outer join tm_wz.wt_mrna_nodes pn
						   on  a.platform = pn.platform
						   and a.category_cd like pn.category_cd || '%'
						   and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'PLATFORM')+8),'TISSUETYPE') > 1 then a.tissue_type else '' end = coalesce(pn.tissue_type,'')
						   and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'PLATFORM')+8),'ATTR1') > 1 then a.attribute_1 else '' end = coalesce(pn.attribute_1,'')
						   and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'PLATFORM')+8),'ATTR2') > 1 then a.attribute_2 else '' end = coalesce(pn.attribute_2,'')
						   and pn.node_type = 'PLATFORM'
				left outer join tm_wz.wt_mrna_nodes ttp
						   on  a.tissue_type = ttp.tissue_type
						   and a.category_cd like ttp.category_cd || '%'
						   and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'TISSUETYPE')+10),'PLATFORM') > 1 then a.platform else '' end = coalesce(ttp.platform,'')
						   and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'TISSUETYPE')+10),'ATTR1') > 1 then a.attribute_1 else '' end = coalesce(ttp.attribute_1,'')
						   and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'TISSUETYPE')+10),'ATTR2') > 1 then a.attribute_2 else '' end = coalesce(ttp.attribute_2,'')
						   and ttp.node_type = 'TISSUETYPE'
				left outer join tm_wz.wt_mrna_nodes a1
						   on  a.attribute_1 = a1.attribute_1
						   and a.category_cd like a1.category_cd || '%'
						   and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'ATTR1')+5),'PLATFORM') > 1 then a.platform else '' end = coalesce(a1.platform,'')
						   and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'ATTR1')+5),'TISSUETYPE') > 1 then a.tissue_type else '' end = coalesce(a1.tissue_type,'')
						   and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'ATTR1')+5),'ATTR2') > 1 then a.attribute_2 else '' end = coalesce(a1.attribute_2,'')
						   and a1.node_type = 'ATTR1'
				left outer join tm_wz.wt_mrna_nodes a2
						   on  a.attribute_2 = a2.attribute_2
						   and a.category_cd like a2.category_cd || '%'
						   and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'ATTR2')+5),'PLATFORM') > 1 then a.platform else '' end = coalesce(a2.platform,'')
						   and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'ATTR2')+5),'TISSUETYPE') > 1 then a.tissue_type else '' end = coalesce(a2.tissue_type,'')
						   and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'ATTR2')+5),'ATTR1') > 1 then a.attribute_1 else '' end = coalesce(a2.attribute_1,'')
						   and a2.node_type = 'ATTR2')
		update deapp.de_subject_sample_mapping ssm
		set concept_code=upd.concept_code
		,sample_type_cd=upd.sample_type_cd
		,timepoint_cd=upd.timepoint_cd
		,tissue_type_cd=upd.tissue_type_cd
		,category_cd=upd.category_cd
		,patient_id=upd.patient_id
		,data_uid=upd.data_uid
		,sample_type=upd.sample_type
		,tissue_type=upd.tissue_type
		,timepoint=upd.timepoint
		,omic_patient_id=upd.patient_id
		,partition_id=partitionId
		from upd
		where ssm.trial_name = TrialID
		and ssm.source_cd = sourceCD
		and coalesce(ssm.site_id,'') = coalesce(upd.site_id,'')
		and ssm.subject_id = upd.subject_id
		and ssm.sample_cd = upd.sample_cd
		and ssm.platform = 'MRNA_AFFYMETRIX';
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update existing data in de_subject_sample_mapping',rowCt,stepCt,'Done');
    pCount := rowCt;	--	set counter to check that all subject_sample mapping records were added/updated

    --	insert any site/subject/samples that are not in de_subject_sample_mapping

    begin
	insert into deapp.de_subject_sample_mapping (
	    patient_id
	    ,site_id
	    ,subject_id
	    ,subject_type
	    ,concept_code
	    ,assay_id
	    ,sample_type
	    ,sample_type_cd
	    ,trial_name
	    ,timepoint
	    ,timepoint_cd
	    ,tissue_type
	    ,tissue_type_cd
	    ,platform
	    ,platform_cd
	    ,data_uid
	    ,gpl_id
	    ,sample_cd
	    ,category_cd
	    ,source_cd
	    ,omic_source_study
	    ,omic_patient_id
	    ,partition_id
	)
	select t.patient_id
	       ,t.site_id
	       ,t.subject_id
	       ,t.subject_type
	       ,t.concept_code
	       ,nextval('deapp.seq_assay_id')
	       ,t.sample_type
	       ,t.sample_type_cd
	       ,t.trial_name
	       ,t.timepoint
	       ,t.timepoint_cd
	       ,t.tissue_type
	       ,t.tissue_type_cd
	       ,t.platform
	       ,t.platform_cd
	       ,t.data_uid
	       ,t.gpl_id
	       ,t.sample_cd
	       ,t.category_cd
	       ,t.source_cd
	       ,t.omic_source_study
	       ,t.omic_patient_id
	       ,partitionId
	  from (select
		    distinct b.patient_num as patient_id
		    ,a.site_id
		    ,a.subject_id
		    ,null as subject_type
		    ,ln.concept_cd as concept_code
		    ,a.tissue_type as tissue_type
		    ,ttp.concept_cd as tissue_type_cd
		    ,a.trial_name
		    ,a.attribute_2 as timepoint
		    ,a2.concept_cd as timepoint_cd
		    ,a.attribute_1 as sample_type
		    ,a1.concept_cd as sample_type_cd
		    ,'MRNA_AFFYMETRIX' as platform
		    ,pn.concept_cd as platform_cd
		    ,ln.concept_cd || '-' || b.patient_num::text as data_uid
		    ,a.platform as gpl_id
		    ,a.sample_cd
		    ,coalesce(a.category_cd,'Biomarker_Data+Gene_Expression+PLATFORM+TISSUETYPE+ATTR1+ATTR2') as category_cd
		    ,a.source_cd
		    ,TrialId as omic_source_study
		    ,b.patient_num as omic_patient_id
		  from tm_lz.lt_src_mrna_subj_samp_map a
		    --Joining to Pat_dim to ensure the IDs match. If not I2B2 won't work.
			   inner join i2b2demodata.patient_dimension b
				   on regexp_replace(TrialID || ':' || coalesce(a.site_id,'') || ':' || a.subject_id,'(::){1,}', ':','g') = b.sourcesystem_cd
			   inner join tm_wz.wt_mrna_nodes ln
				   on a.platform = ln.platform
				   and a.tissue_type = ln.tissue_type
				   and a.category_cd = ln.category_cd
				   and coalesce(a.attribute_1,'') = coalesce(ln.attribute_1,'')
				   and coalesce(a.attribute_2,'') = coalesce(ln.attribute_2,'')
				   and ln.node_type = 'LEAF'
			   left outer join tm_wz.wt_mrna_nodes pn
					       on a.platform = pn.platform
					       and a.category_cd like pn.category_cd || '%'
					       and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'PLATFORM')+8),'TISSUETYPE') > 1 then a.tissue_type else '' end = coalesce(pn.tissue_type,'')
					       and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'PLATFORM')+8),'ATTR1') > 1 then a.attribute_1 else '' end = coalesce(pn.attribute_1,'')
					       and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'PLATFORM')+8),'ATTR2') > 1 then a.attribute_2 else '' end = coalesce(pn.attribute_2,'')
					       and pn.node_type = 'PLATFORM'
			   left outer join tm_wz.wt_mrna_nodes ttp
					      on a.tissue_type = ttp.tissue_type
					      and a.category_cd like ttp.category_cd || '%'
					      and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'TISSUETYPE')+10),'PLATFORM') > 1 then a.platform else '' end = coalesce(ttp.platform,'')
					      and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'TISSUETYPE')+10),'ATTR1') > 1 then a.attribute_1 else '' end = coalesce(ttp.attribute_1,'')
					      and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'TISSUETYPE')+10),'ATTR2') > 1 then a.attribute_2 else '' end = coalesce(ttp.attribute_2,'')
					      and ttp.node_type = 'TISSUETYPE'
			   left outer join tm_wz.wt_mrna_nodes a1
					      on a.attribute_1 = a1.attribute_1
					      and a.category_cd like a1.category_cd || '%'
					      and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'ATTR1')+5),'PLATFORM') > 1 then a.platform else '' end = coalesce(a1.platform,'')
					      and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'ATTR1')+5),'TISSUETYPE') > 1 then a.tissue_type else '' end = coalesce(a1.tissue_type,'')
					      and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'ATTR1')+5),'ATTR2') > 1 then a.attribute_2 else '' end = coalesce(a1.attribute_2,'')
					      and a1.node_type = 'ATTR1'
			   left outer join tm_wz.wt_mrna_nodes a2
					      on a.attribute_2 = a2.attribute_2
					      and a.category_cd like a2.category_cd || '%'
					      and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'ATTR2')+5),'PLATFORM') > 1 then a.platform else '' end = coalesce(a2.platform,'')
					      and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'ATTR2')+5),'TISSUETYPE') > 1 then a.tissue_type else '' end = coalesce(a2.tissue_type,'')
					      and case when tm_cz.instr(substr(a.category_cd,1,tm_cz.instr(a.category_cd,'ATTR2')+5),'ATTR1') > 1 then a.attribute_1 else '' end = coalesce(a2.attribute_1,'')
					      and a2.node_type = 'ATTR2'
		 where a.trial_name = TrialID
		   and a.source_cd = sourceCD
		   and  ln.concept_cd is not null
		   and not exists
		       (select 1 from deapp.de_subject_sample_mapping x
			 where a.trial_name = x.trial_name
			   and coalesce(a.source_cd,'STD') = x.source_cd
			   and x.platform = 'MRNA_AFFYMETRIX'
			   and coalesce(a.site_id,'') = coalesce(x.site_id,'')
			   and a.subject_id = x.subject_id
			   and a.sample_cd = x.sample_cd
		       )) t;
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert trial into de_subject_sample_mapping',rowCt,stepCt,'Done');
    pCount := pCount + rowCt;

    --	check if all records from lt_src_mrna_subj_samp_map were added/updated

    if sCount <> pCount then
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Not all records in lt_src_mrna_subj_samp_map inserted/updated in de_subject_sample_mapping',0,stepCt,'Done');
	perform tm_cz.cz_error_handler (jobID, procedureName, '-1', 'Application raised error');
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
	return -16;
    end if;

    --	Insert records for subjects into observation_fact

    begin
	insert into i2b2demodata.observation_fact (
	    patient_num
	    ,concept_cd
	    ,modifier_cd
	    ,valtype_cd
	    ,tval_char
	    ,sourcesystem_cd
	    ,start_date
	    ,import_date
	    ,valueflag_cd
	    ,provider_id
	    ,location_cd
	    ,units_cd
	)
	select distinct m.patient_id
			,m.concept_code
			,'@'
			,'T' -- Text data type
			,'E'  --Stands for Equals for Text Types
			,m.trial_name
			,'infinity'::timestamp
			,current_timestamp
			,'@'
			,'@'
			,'@'
			,'' -- no units available
	  from  deapp.de_subject_sample_mapping m
	 where m.trial_name = TrialID
	   and m.source_cd = sourceCD
	   and m.platform = 'MRNA_AFFYMETRIX';
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert patient facts into I2B2DEMODATA observation_fact',rowCt,stepCt,'Done');

    --Update I2b2 for correct c_columndatatype, c_visualattributes, c_metadataxml

    begin
	with upd as (select x.concept_cd, min(case when x.node_type = 'LEAF' then 0 else 1 end) as node_type from tm_wz.wt_mrna_nodes x group by x.concept_cd)
		update i2b2metadata.i2b2 t
		set c_columndatatype = 'T'
		,c_metadataxml = null
		,c_visualattributes=case when upd.node_type = 0 then 'LAH' else 'FA' end
		from upd
		where t.c_basecode = upd.concept_cd;
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Initialize data_type, visualattributes and xml in i2b2',rowCt,stepCt,'Done');

    update i2b2metadata.i2b2 a
       set c_visualattributes='FAS'
     where a.c_fullname = substr(topNode,1,tm_cz.instr(topNode,'\',1,3));

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update visual attributes for study nodes in I2B2METADATA i2b2',RowCt,stepCt,'Done');

    --Build concept Counts
    --Also marks any i2B2 records with no underlying data as Hidden, need to do at Trial level because there may be multiple platform and there is no longer
    -- a unique top-level node for mRNA data

    perform tm_cz.i2b2_create_concept_counts(TrialID, topNode, jobID );
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create concept counts',0,stepCt,'Done');

    --	delete each node that is hidden

    FOR r_delNodes in delNodes Loop

	--	deletes hidden nodes for a trial one at a time

	select tm_cz.i2b2_delete_1_node(r_delNodes.c_fullname,jobId) into rtnCd;
	stepCt := stepCt + 1;
	if(rtnCd <> 1) then
	    tText := 'Failed to delete node '|| r_delNodes.c_fullname;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,0,stepCt,'Message');
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
        end if;

	tText := 'Deleted node: ' || r_delNodes.c_fullname;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,0,stepCt,'Done');

    END LOOP;

    --Reload Security: Inserts one record for every I2B2 record into the security table

    select tm_cz.i2b2_load_security_data(TrialID,jobId) into rtnCd;
    stepCt := stepCt + 1;
    if(rtnCd <> 1) then
        perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Failed to load security data',0,stepCt,'Message');
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
	return -16;
    end if;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Load security data',0,stepCt,'Done');

    if (dataType = 'R') then
        begin
            delete from tm_lz.lt_src_mrna_data
	     where intensity_value <= 0.0;
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
        perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Remove unusable negative intensity_value from lt_src_mrna_data for dataType R',rowCt,stepCt,'Done');
    end if;

    --	tag data with probeset_id from reference.

    execute ('truncate table tm_wz.wt_subject_mrna_probeset');

    --	note: assay_id represents a unique subject/site/sample

    begin
        rowCt := 0;
	insert into tm_wz.wt_subject_mrna_probeset (
	    probeset_id
	    ,intensity_value
	    ,assay_id
    	    ,patient_id
	    ,trial_name
	)
	select gs.probeset_id
	       ,avg(md.intensity_value::double precision)
	       ,sd.assay_id
	       ,sd.patient_id
	       ,TrialId
	  from
		  tm_lz.lt_src_mrna_data md
		  inner join deapp.de_subject_sample_mapping sd
		  inner join deapp.de_mrna_annotation gs
			  on sd.gpl_id = gs.gpl_id
			  on md.expr_id = sd.sample_cd
			  and md.probeset = gs.probe_id
	 where sd.platform = 'MRNA_AFFYMETRIX'
	   and sd.trial_name = TrialId
	   and sd.source_cd = sourceCd
	 group by gs.probeset_id
	  	  ,sd.assay_id
          	  ,sd.patient_id;
	get diagnostics thisRowCt := ROW_COUNT;
	rowCt := rowCt + thisRowCt;
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
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert into DEAPP wt_subject_mrna_probeset',rowCt,stepCt,'Done');

    if rowCt = 0 then
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Unable to match probesets to platform in de_mrna_annotation',0,rowCt,'Done');
	perform tm_cz.cz_error_handler (jobID, procedureName, '-1', 'Application raised error');
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
	return -16;
    end if;

    --	insert into de_subject_microarray_data when dataType is T (transformed)

    if dataType = 'T' or dataType = 'Z' then -- Z is for compatibility with TR ETL default settings
        select count(*) into pExists
          from information_schema.tables
         where table_name = partitionIndx;

        if pExists = 0 then
	    sqlText := 'create table ' || partitionName || ' ( constraint mrna_' || partitionId::text || '_check check ( partition_id = ' || partitionId::text ||
	        ')) inherits (deapp.de_subject_microarray_data)';
	    raise notice 'sqlText= %', sqlText;
	    execute sqlText;
	    stepCt := stepCt + 1;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create partition ' || partitionName,1,stepCt,'Done');
	end if;

	sqlText := 'insert into ' || partitionName || ' (partition_id, trial_name, probeset_id, assay_id, patient_id, log_intensity, zscore) ' ||
	    'select ' || partitionId::text || ', trial_name, probeset_id, assay_id, patient_id, intensity_value, ' ||
	    'case when intensity_value < -2.5 then -2.5 when intensity_value > 2.5 then 2.5 else intensity_value end ' ||
	    'from tm_wz.wt_subject_mrna_probeset';
	raise notice 'sqlText= %', sqlText;
	execute sqlText;
	get diagnostics rowCt := ROW_COUNT;
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Inserted data into ' || partitionName,rowCt,stepCt,'Done');
    else
	--	calculate zscore and insert to partition

	if dataType = 'R' or dataType = 'L' then
	    begin
		select tm_cz.i2b2_mrna_zscore_calc(TrialID, partitionName, partitionIndx,partitionId,'L',jobId,dataType,logBase,sourceCD) into rtnCd;
		get diagnostics rowCt := ROW_COUNT;
	        stepCt := stepCt + 1;
	        if(rtnCd <> 1) then
		    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Failed to calculate Z-Score',rowCt,stepCt,'Message');
		    perform tm_cz.cz_end_audit (jobID, 'FAIL');
		    return -16;
	        end if;
	        perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Calculate Z-Score',rowCt,stepCt,'Done');
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
	end if;
    end if;

    ---Cleanup OVERALL JOB if this proc is being run standalone

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'End i2b2_process_mrna_data',0,stepCt,'Done');

    ---Cleanup OVERALL JOB if this proc is being run standalone
    if newJobFlag = 1 then
	perform tm_cz.cz_end_audit (jobID, 'SUCCESS');
    end if;

    return 1;

END;

$$;

