--
-- Name: i2b2_add_snp_biomarker_nodes(character varying, character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_add_snp_biomarker_nodes(trial_id character varying, ont_path character varying, currentjobid numeric DEFAULT 0) RETURNS integer
    LANGUAGE plpgsql
AS $$
    declare

    --	Adds SNP platform and sample type nodes into Biomarker Data ontology and adds rows into observation_fact for
    --	each subject/concept combination

    --	JEA@20110120	New
    --	JEA@@0111218	Remove hard-coded "Biomarker Data" node, use what's supplied in ont_path

    TrialID	varchar(100);
    ontPath		varchar(500);

    RootNode	varchar(300);
    pExists 	integer;
    platformTitle	varchar(200);
    tText		varchar(1000);
    ontLevel	integer;
    nodeName	varchar(200);

    --Audit variables
    newJobFlag numeric(1);
    databaseName varchar(100);
    procedureName varchar(100);
    jobID numeric;
    stepCt integer;
    rowCt  integer;
    rtnCd  integer;

    --	raise exception if platform not in de_gpl_info


    --	cursor to add platform-level nodes, need to be inserted before de_subject_sample_mapping

    addPlatform cursor for
			   select distinct regexp_replace(ont_path || '\' || g.title || '\' ,
							  '(\\){2,}', '\') as path
			   ,g.title
			   from deapp.de_subject_snp_dataset s
			   ,de_gpl_info g
			   where s.trial_name = TrialId
			   and coalesce(s.platform_name,'GPL570') = g.platform
			   and upper(g.organism) = 'HOMO SAPIENS';

    --	cursor to add sample-level nodes

    addSample cursor for
			 select distinct regexp_replace(ont_path || '\' || g.title || '\' ||
							s.sample_type || '\',	'(\\){2,}', '\') as sample_path
			 ,s.sample_type as sample_name
			 from deapp.de_subject_snp_dataset s
			 ,de_gpl_info g
			 where s.trial_name = TrialId
			 and coalesce(s.platform_name,'GPL570') = g.platform
			 and upper(g.organism) = 'HOMO SAPIENS'
			 and (s.sample_type is NOT NULL and s.sample_type::text <> '');

begin
    TrialID := upper(trial_id);
    ontPath := ont_path;

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    perform sys_context('userenv', 'current_schema') INTO databaseName ;
    procedureName := 'i2b2_add_snp_biomarker_nodes';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(coalesce(jobID::text, '') = '' or jobID < 1) then
	newJobFlag := 1; -- True
	perform tm_cz.cz_start_audit (procedureName, databaseName, jobID);
    end if;

    stepCt := 0;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Starting i2b2_add_snp_node',0,stepCt,'Done');
    stepCt := stepCt + 1;

    --	determine last node in ontPath

    perform length(ontPath)-length(replace(ontPath,'\','')) into ontLevel ;
    perform tm_cz.parse_nth_value(ontPath,ontLevel,'\') into nodeName ;

    --	add the high level \ node if it doesn't exist (first time loading data)

    select count(*)
      into pExists
      from i2b2metadata.i2b2
     where c_fullname = regexp_replace(ont_path || '\','(\\){2,}', '\');

    if pExists = 0 then
	select tm_cz.i2b2_add_node(TrialId, regexp_replace(ont_path || '\','(\\){2,}', '\'), nodeName, jobID) into rtnCd;
        stepCt := stepCt + 1;
	if(rtnCd <> 1) then
            tText := 'Failed to add leaf node '|| nodeName;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,0,stepCt,'Message');
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
        end if;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Add node for ontPath',0,stepCt,'Done');
    end if;

    --	check if a node exists for the platform, if yes, then delete existing data, make sure all platforms in de_subject_snp_dataset have an
    --	entry in de_gpl_info, if not, raise exception

    select count(*) into pExists
      from deapp.de_subject_snp_dataset s
	   ,de_gpl_info g
     where s.trial_name = TrialId
       and coalesce(s.platform_name,'GPL570') = g.platform
       and  'HOMO SAPIENS' = upper(g.organism)
       and coalesce(g.platform::text, '') = '';

    if pExists > 0 then
	--	put message in log
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'One or more GPL platforms in de_subject_snp_dataset is not in de_gpl_info',0,stepCt,'Done');

	--End Proc
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
	return -16;
    end if;

    --	add SNP platform nodes

    for r_addPlatform in addPlatform loop

	select tm_cz.i2b2_delete_all_nodes(regexp_replace(ont_path || '\','(\\){2,}', '\') || r_addPlatform.title || '\', jobID) into rtnCd;
	stepCt := stepCt + 1;
	if(rtnCd <> 1) then
	    tText := 'Failed to delete all SNP platform nodes '||regexp_replace(ont_path || '\','(\\){2,}', '\') || r_addPlatform.title || '\';
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,0,stepCt,'Message');
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
        end if;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete existing SNP Platform for trial in I2B2METADATA i2b2',0,stepCt,'Done');

	select tm_cz.i2b2_add_node(TrialId, r_addPlatform.path, r_addPlatform.title, jobId) into rtnCd;
	stepCt := stepCt + 1;
	if(rtnCd <> 1) then
            tText := 'Failed to add leaf node '|| r_addPlatform.path;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,0,stepCt,'Message');
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
        end if;
	tText := 'Added Platform: ' || r_addPlatform.path || '  Name: ' || r_addPlatform.title;
	get diagnostics rowCt := ROW_COUNT;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,rowCt,stepCt,'Done');
    end loop;

    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Added SNP Platform nodes',0,stepCt,'Done');
    stepCt := stepCt + 1;
    commit;

    --	Insert the sample-level nodes

    for r_addSample in addSample loop

	select tm_cz.i2b2_add_node(TrialId, r_addSample.sample_path, r_addSample.sample_name, jobId) into rtnCd;
	if(rtnCd <> 1) then
            tText := 'Failed to add leaf node '|| r_addSample.sample_path;
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,0,stepCt,'Message');
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
        end if;
        tText := 'Added Sample: ' || r_addSample.sample_path || '  Name: ' || r_addSample.sample_name;
 	get diagnostics rowCt := ROW_COUNT;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,RowCt,stepCt,'Done');
	stepCt := stepCt + 1;

    end loop;

    --	Insert records for patients into observation_fact

    insert into i2b2demodata.observation_fact
		(patient_num
		,concept_cd
		,modifier_cd
		,valtype_cd
		,tval_char
		,nval_num
		,sourcesystem_cd
		,start_date
		,import_date
		,valueflag_cd
		,provider_id
		,location_cd
		,units_cd
		)
    select p.patient_num
	   ,t.concept_cd
	   ,t.sourcesystem_cd
	   ,'T' -- Text data type
	   ,'E'  --Stands for Equals for Text Types
	   ,null	--	not numeric for Proteomics
	   ,t.sourcesystem_cd
	   ,'infinity'::timestamp
	   ,localtimestamp
	   ,'@'
	   ,'@'
	   ,'@'
	   ,'' -- no units available
      from  i2b2demodata.concept_dimension t
	    ,deapp.de_subject_snp_dataset p
	    ,deapp.de_gpl_info g
     where p.trial_name =  TrialId
       and coalesce(p.platform_name,'GPL570') = g.platform
       and upper(g.organism) = 'HOMO SAPIENS'
       and t.concept_path = regexp_replace(ont_path || '\','(\\){2,}', '\') || g.title || '\' || p.sample_type || '\'
     group by p.patient_num
	      ,t.concept_cd
	      ,t.sourcesystem_cd;
    get diagnostics rowCt := ROW_COUNT;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert trial into I2B2DEMODATA observation_fact',rowCt,stepCt,'Done');
    stepCt := stepCt + 1;
    commit;

    --	update concept_cd in de_subject_snp_dataset

    update deapp.de_subject_snp_dataset d
       set concept_cd = (select t.concept_cd
			   from deapp.de_subject_snp_dataset p
				,deapp.de_gpl_info g
				,i2b2demodata.concept_dimension t
			  where d.subject_snp_dataset_id = p.subject_snp_dataset_id
			    and coalesce(p.platform_name,'GPL570') = g.platform
			    and upper(g.organism) = 'HOMO SAPIENS'
			    and t.concept_path = regexp_replace(ont_path || '\','(\\){2,}', '\') || g.title || '\' || p.sample_type || '\'
       )
     where d.trial_name = TrialId;

    stepCt := stepCt + 1;  get diagnostics rowCt := ROW_COUNT;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update concept_cd in DEAPP de_subject_snp_dataset',rowCt,stepCt,'Done');
    commit;

    --	Update visual attributes for leaf active (default is folder)

    update i2b2metadata.i2b2 a
       set c_visualattributes = 'LA'
     where 1 = (select count(*)
		  from i2b2metadata.i2b2 b
		 where b.c_fullname like (a.c_fullname || '%'))
	   and a.c_fullname like regexp_replace(ont_path || '\','(\\){2,}', '\') || '%';
    stepCt := stepCt + 1;  get diagnostics rowCt := ROW_COUNT;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update leaf active attribute for trial in I2B2METADATA i2b2',rowCt,stepCt,'Done');
    commit;

    --	fill in tree

    --	get top level for study, this will be used for fill-in and create_concept_counts
    --	if this fails, check to make sure the trialId is not a sourcesystem_cd at an higher level than the study

    select b.c_fullname into nodeName
      from i2b2metadata.i2b2 b
     where b.c_hlevel =
	   (select min(x.c_hlevel) from i2b2metadata.i2b2 x
	     where b.sourcesystem_cd = x.sourcesystem_cd)
       and ontPath like b.c_fullname || '%'
       and b.sourcesystem_cd = TrialId;

    select tm_cz.i2b2_fill_in_tree(TrialID,regexp_replace(nodeName || '\','(\\){2,}', '\'), jobID) into rtnCd;
    stepCt := stepCt + 1;  get diagnostics rowCt := ROW_COUNT;
    if(rtnCd <> 1) then
        tText := 'Failed to fill in tree '|| regexp_replace(nodeName || '\','(\\){2,}', '\');
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,0,stepCt,'Message');
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
	return -16;
    end if;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Fill in tree for Biomarker Data for trial',rowCt,stepCt,'Done');

    --Build concept Counts
    --Also marks any i2B2 records with no underlying data as Hidden, need to do at Biomarker level because there may be multiple platforms and patient count can vary

    perform tm_cz.i2b2_create_concept_counts(TrialID, regexp_replace(nodeName || '\','(\\){2,}', '\'),jobID );
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Create concept counts',0,stepCt,'Done');

    --Reload Security: Inserts one record for every I2B2 record into the security table

    select tm_cz.i2b2_load_security_data(TrialID,jobId) into rtnCd;
    if(rtnCd <> 1) then
        stepCt := stepCt + 1;
        perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Failed to load security data',0,stepCt,'Message');
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
	return -16;
    end if;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Load security data',0,stepCt,'Done');

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'End i2b2_process_protein_data',0,stepCt,'Done');

    ---Cleanup OVERALL JOB if this proc is being run standalone
    if newJobFlag = 1 then
	perform tm_cz.cz_end_audit (jobID, 'SUCCESS');
    end if;

   return 1;
exception

    when others then
    --Handle errors.
	perform tm_cz.cz_error_handler(jobId, procedureName, SQLSTATE, SQLERRM);
    --End Proc
	perform tm_cz.cz_end_audit (jobID, 'FAIL');

    return -16;
end;

$$;

