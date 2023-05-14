--
-- Name: i2b2_backout_trial(character varying, character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_backout_trial(trialid character varying, path_string character varying, currentjobId numeric) RETURNS integer
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

    declare

    pExists		integer;
    sqlTxt		character varying;
    msgTxt		character varying;
    topNode		character varying;
    v_partition_id	text;
    secureObjId		bigint;

    --Audit variables
    newJobFlag		integer;
    databaseName	VARCHAR(100);
    procedureName	VARCHAR(100);
    jobId 		numeric(18,0);
    stepCt 		numeric(18,0);
    rowCt		numeric(18,0);
    errorNumber		character varying;
    errorMessage	character varying;
    auditMessage	character varying;
    rtnCd		integer;

begin

    stepCt := 0;

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobId := currentjobId;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_backout_trial';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(jobId IS NULL or jobId < 1) then
	newJobFlag := 1; -- True
	select tm_cz.cz_start_audit (procedureName, databaseName) into jobId;
    end if;

    if (trialid is null OR length(trialid) = 0) then
	errorNumber := '';
	errorMessage := 'Invalid value for trialid argument';
	--Handle errors.
	perform tm_cz.cz_error_handler (jobId, procedureName, errorNumber, errorMessage);
	--End Proc
	perform tm_cz.cz_end_audit (jobId, 'FAIL');
	return -16;
    end if;

    -- trial ids are stored in upper case in the database
    trialid := upper(trialid);

    -- The second argument for this stored procedure (path_string) is deprecated.
    -- Its value is not independent of the value of the first argument (trialid) and
    -- therefore could be retrieved from the database.
    -- This prevents that errors are introduced in case inconsistent values are used for those arguments.
    -- For now, if a value is provided for path_string, it is checked against the information in the database
    -- and if there is no match, the procedure will be aborted.

    rowCt := 0;
    stepCt := stepCt + 1;
    if (path_string is not null AND length(path_string) > 0) then
	auditMessage := 'The use of the path_string argument for this stored procedure (tm_cz.i2b2_backout_trial) is deprecated';
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,auditMessage,0,stepCt,'Done');
    end if;

    -- I2B2 has an entry in tm_trial_nodes for the absolute top level
    -- Do not allow it to be passed or all data could be deleted
    if(trialid = 'I2B2') then
	perform tm_cz.cz_write_error(jobId,databaseName,procedureName,'Error: I2B2 is not a tranSMART trial',0,stepCt,'FAIL');
	return -16;
    end if;

    -- retrieve topNode for study with given trial id (trialid)
    -- ========================================================

    begin
	select c_fullname from i2b2metadata.tm_trial_nodes where trial = trialid into topNode;
	get diagnostics rowCt := ROW_COUNT;
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

    -- check validity of topNode value
    -- ===============================

    rowCt := 0;
    stepCt := stepCt + 1;
    if (topNode is null OR length(topNode) = 0) then
        -- Either due to erroneous trialid or inconsistent database content
        -- In case of erroneous trialid, the database does not contain any data associated with this trialid (trying to remove does not harm)
        -- In case of inconsistencies in the database, we still might try to remove data associated with this trialid.
	auditMessage := 'Not able to retrieve top node associated with trial id: ' || trialid;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,auditMessage,0,stepCt,'Done');
	end if;

    -- check consistency between topNode and path_string
    -- =================================================

    rowCt := 0;
    stepCt := stepCt + 1;
    if (path_string is not null AND length(path_string) > 0 AND topNode is not null AND path_string != topNode) then
	errorNumber := '';
	errorMessage := 'Discrepancy between path_string argument value ('||path_string||') and value found in database ('||topNode||')';
	--Handle errors.
	perform tm_cz.cz_error_handler (jobId, procedureName, errorNumber, errorMessage);
	--End Proc
	perform tm_cz.cz_end_audit (jobId, 'FAIL');
	return -16;
    end if;

    -- delete all i2b2 nodes
    -- =====================

    rowCt := 0;
    stepCt := stepCt + 1;
    if (topNode is null OR length(topNode) = 0) then
	auditMessage := 'Not able to retrieve top node associated with trial id: ' || trialid;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,auditMessage,0,stepCt,'Done');

	auditMessage := 'Start deleting all data for trial ' || trialid;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,auditMessage,0,stepCt,'Done');
    else
	auditMessage := 'Start deleting all data for trial ' || trialid || ' and topNode ' || topNode;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,auditMessage,0,stepCt,'Done');

	select tm_cz.i2b2_delete_all_nodes(topNode,jobId) into rtnCd;
	if(rtnCd <> 1) then
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Failed to delete all nodes',0,stepCt,'Message');
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
        end if;
    end if;

    -- delete clinical data
    -- ====================

    begin
	delete from tm_lz.lz_src_clinical_data
	 where study_id = trialid;
	get diagnostics rowCt := ROW_COUNT;
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
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete data for trial from lz_src_clinical_data',rowCt,stepCt,'Done');

    -- delete observation_fact SECURITY data, do before patient_dimension delete
    -- =========================================================================
    begin
	delete from i2b2demodata.observation_fact f
	 where f.concept_cd = 'SECURITY'
	       and f.patient_num in
	       (select distinct p.patient_num from i2b2demodata.patient_dimension p
		 where p.sourcesystem_cd like trialid || ':%');
	get diagnostics rowCt := ROW_COUNT;
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
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete SECURITY data for trial from I2B2DEMODATA observation_fact',rowCt,stepCt,'Done');

    -- delete encounter data
    -- =====================

    begin
	delete from i2b2demodata.encounter_mapping
	 where sourcesystem_cd like trialid || ':%';
	get diagnostics rowCt := ROW_COUNT;
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
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete data for trial from I2B2DEMODATA encounter_mapping',rowCt,stepCt,'Done');

    -- delete patient data
    -- ===================

    begin
	delete from i2b2demodata.patient_mapping
	 where sourcesystem_cd like trialid || ':%';
	get diagnostics rowCt := ROW_COUNT;
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
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete data for trial from I2B2DEMODATA patient_mapping',rowCt,stepCt,'Done');

    begin
	delete from i2b2demodata.patient_dimension
	 where sourcesystem_cd like trialid || ':%';
	get diagnostics rowCt := ROW_COUNT;
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
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete data for trial from I2B2DEMODATA patient_dimension',rowCt,stepCt,'Done');

    begin
	delete from i2b2demodata.patient_trial
	 where trial=  trialid;
	get diagnostics rowCt := ROW_COUNT;
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
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete data for trial from I2B2DEMODATA patient_trial',rowCt,stepCt,'Done');

    -- High-dimensional data types
    -- ===========================

    --	delete gene expression data
    --  ===========================

    select count(*) into pExists
      from deapp.de_subject_sample_mapping
     where trial_name = TrialId
       and platform = 'MRNA_AFFYMETRIX'
       and trial_name = TrialId
       and coalesce(omic_source_study,trial_name) = TrialId;

    if pExists > 0 then
        rowCt := 0;
	for v_partition_id in
	    select distinct partition_id::text
	    from deapp.de_subject_sample_mapping
	    where trial_name = TrialId
	    and platform = 'MRNA_AFFYMETRIX'
	    and coalesce(omic_source_study,trial_name) = TrialId
	    loop
	    sqlTxt := 'drop table if exists deapp.de_subject_microarray_data_' || v_partition_id;
	    execute sqlTxt;
	    stepCt := stepCt + 1;
	    msgTxt := 'Drop partition table '|| v_partition_id || ' for de_subject_microarray_data';
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,msgTxt,rowCt,stepCt,'Done');
	end loop;

	begin
	    delete from deapp.de_subject_sample_mapping
	     where trial_name = TrialID
		   and platform = 'MRNA_AFFYMETRIX';
	    get diagnostics rowCt := ROW_COUNT;
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
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete expression data for trial from DEAPP de_subject_sample_mapping',rowCt,stepCt,'Done');

    end if;

    --	delete acgh data
    --  ================

    select count(*) into pExists
      from deapp.de_subject_sample_mapping
     where trial_name = TrialId
       and platform = 'ACGH'
       and trial_name = TrialId
       and coalesce(omic_source_study,trial_name) = TrialId;

    if pExists > 0 then
	rowCt := 0;

	for v_partition_id in
	    select distinct partition_id::text
	    from deapp.de_subject_sample_mapping
	    where trial_name = TrialId
	    and platform = 'ACGH'
	    and coalesce(omic_source_study,trial_name) = TrialId
	    loop
	    sqlTxt := 'drop table if exists deapp.de_subject_acgh_data_' || v_partition_id;
	    execute sqlTxt;
	    stepCt := stepCt + 1;
	    msgTxt := 'Drop partition table '|| v_partition_id || ' for de_subject_acgh_data';
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,msgTxt,rowCt,stepCt,'Done');
	end loop;

	begin
	    delete from deapp.de_subject_sample_mapping
	     where trial_name = TrialID
		   and platform = 'ACGH';
	    get diagnostics rowCt := ROW_COUNT;
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
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete acgh data for trial from DEAPP de_subject_sample_mapping',rowCt,stepCt,'Done');

    end if;

    --	delete metabolomics data
    -- =========================

    select count(*) into pExists
      from deapp.de_subject_sample_mapping
     where trial_name = TrialId
       and platform = 'METABOLOMICS'
       and trial_name = TrialId
       and coalesce(omic_source_study,trial_name) = TrialId;

    if pExists > 0 then
        rowCt := 0;
	for v_partition_id in
	    select distinct partition_id::text
	    from deapp.de_subject_sample_mapping
	    where trial_name = TrialId
	    and platform = 'METABOLOMICS'
	    and coalesce(omic_source_study,trial_name) = TrialId
	    loop
	    sqlTxt := 'drop table if exists deapp.de_subject_metabolomics_data_' || v_partition_id;
	    execute sqlTxt;
	    stepCt := stepCt + 1;
	    msgTxt := 'Drop partition table '|| v_partition_id || ' for de_subject_metabolomics_data';
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,msgTxt,rowCt,stepCt,'Done');
	end loop;

	begin
	    delete from deapp.de_subject_sample_mapping
	     where trial_name = TrialID
		   and platform = 'METABOLOMICS';
	    get diagnostics rowCt := ROW_COUNT;
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
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete metabolomics data for trial from DEAPP de_subject_sample_mapping',rowCt,stepCt,'Done');

    end if;

    -- delete miRNA qPCR (or miRNAseq) data -- they use the same platform type
    -- =======================================================================

    select count(*) into pExists
      from deapp.de_subject_sample_mapping
     where trial_name = TrialId
       and platform = 'MIRNA_QPCR'
       and trial_name = TrialId
       and coalesce(omic_source_study,trial_name) = TrialId;

    if pExists > 0 then

        delete from deapp.de_subject_mirna_data
               where trial_name = TrialId;
	get diagnostics rowCt := ROW_COUNT;

	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Drop unpartitioned trial data from de_subject_mirna_data',rowCt,stepCt,'Done');

	begin
	    delete from deapp.de_subject_sample_mapping
	     where trial_name = TrialID
		   and platform = 'MIRNA_QPCR';
	    get diagnostics rowCt := ROW_COUNT;
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
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete mirnaqpcr expression data for trial from DEAPP de_subject_sample_mapping',rowCt,stepCt,'Done');

    end if;

    -- delete (ms)proteomics data
    -- =======================

    select count(*) into pExists
      from deapp.de_subject_sample_mapping
     where trial_name = TrialId
       and platform = 'PROTEIN'
       and trial_name = TrialId
       and coalesce(omic_source_study,trial_name) = TrialId;

    if pExists > 0 then

        delete from deapp.de_subject_protein_data
               where trial_name = TrialId;
	get diagnostics rowCt := ROW_COUNT;

	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Drop unpartitioned trial data from de_subject_protein_data',rowCt,stepCt,'Done');

	begin
	    delete from deapp.de_subject_sample_mapping
	     where trial_name = TrialID
		   and platform = 'PROTEIN';
	    get diagnostics rowCt := ROW_COUNT;
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
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete proteomics data for trial from DEAPP de_subject_sample_mapping',rowCt,stepCt,'Done');

    end if;

    -- delete rnaseq data with chromosomal regions
    -- ===========================================

    select count(*) into pExists
      from deapp.de_subject_sample_mapping
     where trial_name = TrialId
       and platform = 'RNASEQ'
       and trial_name = TrialId
       and coalesce(omic_source_study,trial_name) = TrialId;

    if pExists > 0 then
        rowCt := 0;
	for v_partition_id in
	    select distinct partition_id::text
	    from deapp.de_subject_sample_mapping
	    where trial_name = TrialId
	    and platform = 'RNASEQ'
	    and coalesce(omic_source_study,trial_name) = TrialId
	    loop
	    sqlTxt := 'drop table if exists deapp.de_subject_rnaseq_data_' || v_partition_id;
	    execute sqlTxt;
	    stepCt := stepCt + 1;
	    msgTxt := 'Drop partition table '|| v_partition_id || ' for de_subject_rnaseq_data';
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,msgTxt,rowCt,stepCt,'Done');
	end loop;

	begin
	    delete from deapp.de_subject_sample_mapping
	     where trial_name = TrialID
		   and platform = 'RNASEQ';
	    get diagnostics rowCt := ROW_COUNT;
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
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete rnaseq data for trial from DEAPP de_subject_sample_mapping',rowCt,stepCt,'Done');

    end if;

    -- delete rnaseq expression data
    -- =============================

    select count(*) into pExists
      from deapp.de_subject_sample_mapping
     where trial_name = TrialId
       and platform = 'RNASEQCOG'
       and trial_name = TrialId
       and coalesce(omic_source_study,trial_name) = TrialId;

    if pExists > 0 then
        rowCt := 0;
	for v_partition_id in
	    select distinct partition_id::text
	    from deapp.de_subject_sample_mapping
	    where trial_name = TrialId
	    and platform = 'RNASEQCOG'
	    and coalesce(omic_source_study,trial_name) = TrialId

	    loop
	        sqlTxt := 'drop table if exists deapp.de_subject_rna_data_' || v_partition_id;
	        execute sqlTxt;
	        stepCt := stepCt + 1;
	        perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Drop partition table for de_subject_rna_data',rowCt,stepCt,'Done');
	    end loop;

	begin
	    delete from deapp.de_subject_sample_mapping
	     where trial_name = TrialID
		   and platform = 'RNASEQCOG';
	    get diagnostics rowCt := ROW_COUNT;
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
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete rnaseq expression data for trial from DEAPP de_subject_sample_mapping',rowCt,stepCt,'Done');

    end if;

    -- delete rbm data
    -- ===============

    select count(*) into pExists
      from deapp.de_subject_sample_mapping
     where trial_name = TrialId
       and platform = 'RBM'
       and trial_name = TrialId
       and coalesce(omic_source_study,trial_name) = TrialId;

    if pExists > 0 then
	rowCt := 0;
	for v_partition_id in
	    select distinct partition_id::text
	    from deapp.de_subject_sample_mapping
	    where trial_name = TrialId
	    and platform = 'RBM'
	    and coalesce(omic_source_study,trial_name) = TrialId
	    loop
	    sqlTxt := 'drop table if exists deapp.de_subject_rbm_data_' || v_partition_id;
	    execute sqlTxt;
	    stepCt := stepCt + 1;
	    msgTxt := 'Drop partition table '|| v_partition_id || ' for de_subject_rbm_data';
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,msgTxt,rowCt,stepCt,'Done');
	end loop;

	begin
	    delete from deapp.de_subject_sample_mapping
	     where trial_name = TrialID
		   and platform = 'RBM';
	    get diagnostics rowCt := ROW_COUNT;
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
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete rbm data for trial from DEAPP de_subject_sample_mapping',rowCt,stepCt,'Done');

    end if;

    -- other tables associated with trials
    -- ===================================

    -- delete from search_secure_object
    -- ================================

    select count(*) into pExists
      from searchapp.search_secure_object
     where bio_data_unique_id = 'EXP:' || TrialId;

    if pExists > 0 then

	-- delete possible access right related to this secure object(s)
	delete from searchapp.search_auth_sec_object_access
	where secure_object_id in
	( select search_secure_object_id from searchapp.search_secure_object
	   where bio_data_unique_id = 'EXP:' || TrialId
	);

	-- delete the secure object(s)
	delete from searchapp.search_secure_object
	 where bio_data_unique_id = 'EXP:' || TrialId;

	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete trial from search_secure_object',1,stepCt,'Done');

    end if;

    delete from i2b2metadata.tm_trial_nodes where trial = trialid;
    get diagnostics rowCt := ROW_COUNT;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete trial from tm_trial_nodes',rowCt,stepCt,'Done');

    
    -- Checks for possible errors, or notes for deleted partially removed trial
    -- ========================================================================

    -- Report potential erroneous value for trialid because topNode could not be found
    -- ===============================================================================

    if (topNode is null OR length(topNode) = 0) then
	auditMessage := 'trialid argument possibly contained erroneous value ' || trialid;
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,auditMessage,0,stepCt,'Done');
	perform tm_cz.cz_end_audit (jobId, 'WARNING');
	return -1;
    end if;

    ---Cleanup OVERALL JOB if this proc is being run standalone

    if newJobFlag = 1 then
	perform tm_cz.cz_end_audit (jobId, 'SUCCESS');
    end if;

    return 1;

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

