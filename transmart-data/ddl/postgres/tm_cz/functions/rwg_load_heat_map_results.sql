-----------------------------------------------------------------------
--             DO NOT EDIT THIS FILE. IT IS AUTOGENERATED            --
-- Edit the original file in the macroed_functions directory instead --
-----------------------------------------------------------------------
-- Generated by Ora2Pg, the Oracle database Schema converter, version 11.4
-- Copyright 2000-2013 Gilles DAROLD. All rights reserved.
-- DATASOURCE: dbi:Oracle:host=mydb.mydom.fr;sid=SIDNAME

CREATE OR REPLACE FUNCTION tm_cz.rwg_load_heat_map_results (
  In_Study_Id In text
  ,currentJobID bigint DEFAULT null
)  RETURNS bigint AS $body$
DECLARE

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

	--Audit variables
	newJobFlag    smallint;
	databaseName  varchar(100);
	procedureName varchar(100);
	jobID         bigint;
	stepCt        bigint;
	rowCt         bigint;
	errorNumber   varchar;
	errorMessage  varchar;

	sqlText    varchar(500);
	partExists boolean;
	partTable  text;
	i          integer;
	/*
	cursor cInsert is
	Select Distinct Decode(B1.Study_Id, 'C0524T03_RWG', 'C0524T03', B1.Study_Id) Study_Id, B1.Bio_Assay_Analysis_Id, Cohort_Id
	from biomart.bio_analysis_attribute b1, biomart.bio_analysis_cohort_xref b2
	Where B1.Bio_Assay_Analysis_Id = B2.Bio_Assay_Analysis_Id
	and upper(b1.study_id)=upper(in_study_id);
	Cursor Cdelete Is
	Select Distinct Bio_Assay_Analysis_Id
	From biomart.Heat_Map_Results
	where upper(trial_name) = upper(in_study_id);
	cursor ctaIds is
	select bio_assay_analysis_id
	from biomart.bio_assay_analysis
	where etl_id = upper(in_study_id) || ':RWG';
	cInsertRow cInsert%rowtype;
	cDeleteRow cDelete%rowtype;
	cCtaId ctaIds%rowtype;
	 */

BEGIN
	--Set Audit Parameters
	newJobFlag := 0; -- False (Default)
	jobID := currentJobID;
	select current_user INTO databaseName; --(sic)
	procedureName := 'rwg_load_heat_map_results';

	--Audit JOB Initialization
	--If Job ID does not exist, then this is a single procedure run and we need to create it
	IF (coalesce(jobID::text, '') = '' OR jobID < 1)
		THEN
		newJobFlag := 1; -- True
		SELECT tm_cz.cz_start_audit(procedureName, databaseName) INTO jobID;
	END IF;
	perform tm_cz.cz_write_audit(jobId, databaseName, procedureName,
		'Start FUNCTION', 0, stepCt, 'Done');
	stepCt := 1;

	partTable := 'heat_map_results_' || lower(in_study_id);

	--	check if partition exists
	SELECT
		EXISTS (
			SELECT
				*
			FROM
				pg_tables
			WHERE
				schemaname = 'biomart'
				AND tablename = partTable )
		INTO partExists;

	IF NOT partExists THEN
		--	needed to add partition to table
		sqlText := 'CREATE TABLE biomart.' || partTable || '(' ||
			'CHECK (trial_name = ''' || upper(in_study_id) || ''') ' ||
			')'
			'INHERITS (biomart.heat_map_results) ' ||
			'TABLESPACE indx';

		BEGIN
		EXECUTE(sqlText);
		perform tm_cz.cz_write_audit(jobId, databaseName, procedureName,
		'Adding partition to BIOMART.HEAT_MAP_RESULTS', 0, stepCt, 'Done');
	stepCt := stepCt + 1;
	EXCEPTION
		WHEN OTHERS THEN
		errorNumber := SQLSTATE;
		errorMessage := SQLERRM;
		perform tm_cz.cz_error_handler(jobID, procedureName, errorNumber, errorMessage);
		perform tm_cz.cz_end_audit (jobID, 'FAIL');
		RETURN -16;
	END;

		sqlText := 'CREATE OR REPLACE RULE ' || partTable || '_insert AS ' ||
			'ON INSERT TO biomart.heat_map_results WHERE ' ||
			'(trial_name = ''' || upper(in_study_id) || ''') ' ||
			'DO INSTEAD INSERT INTO biomart.' || partTable || ' ' ||
			'VALUES(NEW.*)';
		BEGIN
		EXECUTE(sqlText);
		perform tm_cz.cz_write_audit(jobId, databaseName, procedureName,
		'Adding rule to main table', 0, stepCt, 'Done');
	stepCt := stepCt + 1;
	EXCEPTION
		WHEN OTHERS THEN
		errorNumber := SQLSTATE;
		errorMessage := SQLERRM;
		perform tm_cz.cz_error_handler(jobID, procedureName, errorNumber, errorMessage);
		perform tm_cz.cz_end_audit (jobID, 'FAIL');
		RETURN -16;
	END;

		-- this index is small and very effective for making the UI responsive
		sqlText := 'CREATE INDEX ' || partTable || '_sign_index ' ||
			'ON biomart.' || partTable || '(significant) ' ||
			'TABLESPACE indx';
		BEGIN
		EXECUTE(sqlText);
		perform tm_cz.cz_write_audit(jobId, databaseName, procedureName,
		'Adding index to partition', 0, stepCt, 'Done');
	stepCt := stepCt + 1;
	EXCEPTION
		WHEN OTHERS THEN
		errorNumber := SQLSTATE;
		errorMessage := SQLERRM;
		perform tm_cz.cz_error_handler(jobID, procedureName, errorNumber, errorMessage);
		perform tm_cz.cz_end_audit (jobID, 'FAIL');
		RETURN -16;
	END;
	ELSE
		--truncate partition
		sqlText := 'TRUNCATE TABLE biomart.' || partTable;

		BEGIN
		EXECUTE(sqlText);
		GET DIAGNOSTICS rowCt := ROW_COUNT;
	perform tm_cz.cz_write_audit(jobId, databaseName, procedureName,
		'Truncate partition in BIOMART.HEAT_MAP_RESULTS', rowCt, stepCt, 'Done');
	stepCt := stepCt + 1;
	EXCEPTION
		WHEN OTHERS THEN
		errorNumber := SQLSTATE;
		errorMessage := SQLERRM;
		perform tm_cz.cz_error_handler(jobID, procedureName, errorNumber, errorMessage);
		perform tm_cz.cz_end_audit (jobID, 'FAIL');
		RETURN -16;
	END;
	END IF;

	--	Delete existing data for study from cta_results
	BEGIN
	DELETE
	FROM
		biomart.cta_results
	WHERE
		bio_assay_analysis_id IN (
			SELECT
				x.bio_assay_analysis_id
			FROM
				biomart.bio_assay_analysis x
			WHERE
				x.etl_id = UPPER ( in_study_id ) || ':RWG' );

	GET DIAGNOSTICS rowCt := ROW_COUNT;
	perform tm_cz.cz_write_audit(jobId, databaseName, procedureName,
		'Delete records for study from cta_results', rowCt, stepCt, 'Done');
	stepCt := stepCt + 1;
	EXCEPTION
		WHEN OTHERS THEN
		errorNumber := SQLSTATE;
		errorMessage := SQLERRM;
		perform tm_cz.cz_error_handler(jobID, procedureName, errorNumber, errorMessage);
		perform tm_cz.cz_end_audit (jobID, 'FAIL');
		RETURN -16;
	END;

	--	changed to use sql instead of view, view pulled back all studies   20121203 JEA
	BEGIN
	INSERT INTO biomart.heat_map_results (
		subject_id,
		log_intensity,
		cohort_id,
		probe_id,
		bio_assay_feature_group_id,
		fold_change_ratio,
		tea_normalized_pvalue,
		bio_marker_name,
		bio_marker_id,
		search_keyword_id,
		bio_assay_analysis_id,
		trial_name,
		significant,
		gene_id,
		assay_id,
		preferred_pvalue )
	SELECT
		REPLACE ( REPLACE ( pd.sourcesystem_cd, xref.study_id, '' ),
			':', '' ) AS subject_id,
		md.log_intensity,
		cex.cohort_id,
		dma.probeset,
		baad.bio_assay_feature_group_id,
		baad.Fold_Change_Ratio,
		baad.tea_normalized_pvalue AS tea_normalized_pvalue,
		f.bio_marker_name,
		f.bio_marker_id,
		i.SEARCH_KEYWORD_ID,
		xref.bio_assay_analysis_id,
		xref.study_id,
		CASE
			WHEN ( ABS ( baaD.Fold_Change_Ratio ) > baa.Fold_Change_Cutoff
				OR COALESCE ( baaD.Fold_Change_Ratio ::TEXT, '' ) = '' )
			AND COALESCE ( baad.preferred_pvalue, baad.tea_normalized_pvalue )
				< baa.pvalue_cutoff
			AND ( ( baad.lsmean1 > baa.lsmean_cutoff
					OR baad.lsmean2 > baa.lsmean_cutoff )
				OR ( COALESCE ( baad.lsmean1 ::TEXT, '' ) = ''
					AND COALESCE ( baad.lsmean2 ::TEXT, '' ) = '' ) )
			THEN 1
			ELSE 0
		END Significant,
		f.Primary_External_Id,
		sm.assay_id,
		baad.preferred_pvalue
	FROM
		biomart.bio_analysis_cohort_xref xref
	INNER
		JOIN biomart.bio_cohort_exp_xref cex ON xref.study_id = cex.study_id
		AND xref.cohort_id = cex.cohort_id
	INNER
		JOIN deapp.de_subject_sample_mapping sm ON xref.study_id = sm.trial_name
		AND cex.exp_id = sm.assay_id::varchar
	INNER
		JOIN deapp.de_subject_microarray_data md ON md.trial_name = xref.study_id
		AND cex.exp_id = md.assay_id::varchar
	INNER
		JOIN tm_cz.probeset_deapp dma -- use tm_cz.probeset_deapp because there is only a single record for the probeset
		ON md.probeset_id = dma.probeset_id
	INNER
		JOIN i2b2demodata.patient_dimension pd ON sm.patient_id = pd.patient_num
	INNER
		JOIN biomart.bio_assay_analysis_data baad ON xref.bio_assay_analysis_id = baad.bio_assay_analysis_id
		AND baad.feature_group_name = dma.probeset
	INNER JOIN biomart.bio_assay_data_annotation e ON e.bio_assay_feature_group_id = baad.bio_assay_feature_group_id
	INNER JOIN biomart.bio_marker f ON f.bio_marker_id = e.bio_marker_id
	INNER
		JOIN biomart.bio_assay_analysis baa ON xref.bio_assay_analysis_id = baa.bio_assay_analysis_id
	INNER
		JOIN biomart.bio_marker_correl_mv h ON f.bio_marker_id = h.asso_bio_marker_id
		AND h.correl_type IN ( 'GENE', 'HOMOLOGENE_GENE', 'PROTEIN TO GENE' )
	INNER
		JOIN searchapp.search_keyword i ON f.bio_marker_id = i.bio_data_id
	WHERE
		xref.study_id = UPPER ( in_study_id );

	GET DIAGNOSTICS rowCt := ROW_COUNT;
	perform tm_cz.cz_write_audit(jobId, databaseName, procedureName,
		'Insert study to heat_map_results', rowCt, stepCt, 'Done');
	stepCt := stepCt + 1;
	EXCEPTION
		WHEN OTHERS THEN
		errorNumber := SQLSTATE;
		errorMessage := SQLERRM;
		perform tm_cz.cz_error_handler(jobID, procedureName, errorNumber, errorMessage);
		perform tm_cz.cz_end_audit (jobID, 'FAIL');
		RETURN -16;
	END;

	BEGIN
	UPDATE BIOMART.bio_assay_analysis baa
	SET
		analysis_update_date = now()
	WHERE
		baa.bio_assay_analysis_id IN (
			SELECT
				x.bio_assay_analysis_id
			FROM
				biomart.bio_assay_analysis x
			WHERE
				x.etl_id = UPPER ( in_study_id ) || ':RWG' );

	GET DIAGNOSTICS rowCt := ROW_COUNT;
	perform tm_cz.cz_write_audit(jobId, databaseName, procedureName,
		'Updated analysis_update_date for analyses of study', rowCt, stepCt, 'Done');
	stepCt := stepCt + 1;
	EXCEPTION
		WHEN OTHERS THEN
		errorNumber := SQLSTATE;
		errorMessage := SQLERRM;
		perform tm_cz.cz_error_handler(jobID, procedureName, errorNumber, errorMessage);
		perform tm_cz.cz_end_audit (jobID, 'FAIL');
		RETURN -16;
	END;

	BEGIN
	INSERT INTO biomart.cta_results (
		bio_assay_analysis_id,
		search_keyword_id,
		keyword,
		bio_marker_id,
		bio_marker_name,
		gene_id,
		probe_id,
		fold_change,
		preferred_pvalue,
		organism )
	SELECT
		DISTINCT h.bio_assay_analysis_id,
		h.search_keyword_id,
		UPPER ( s.keyword ),
		h.bio_marker_id,
		b.bio_marker_name,
		b.primary_external_id,
		h.probe_id,
		h.fold_change_ratio,
		h.preferred_pvalue,
		b.organism
	FROM
		biomart.heat_map_results h,
		biomart.bio_marker b,
		searchapp.search_keyword s
	WHERE
		h.trial_name = UPPER ( in_study_id )
		AND h.bio_marker_id = b.bio_marker_id
		AND h.search_keyword_id = s.search_keyword_id;

	GET DIAGNOSTICS rowCt := ROW_COUNT;
	perform tm_cz.cz_write_audit(jobId, databaseName, procedureName,
		'Insert records for study into cta_results', rowCt, stepCt, 'Done');
	stepCt := stepCt + 1;
	EXCEPTION
		WHEN OTHERS THEN
		errorNumber := SQLSTATE;
		errorMessage := SQLERRM;
		perform tm_cz.cz_error_handler(jobID, procedureName, errorNumber, errorMessage);
		perform tm_cz.cz_end_audit (jobID, 'FAIL');
		RETURN -16;
	END;

	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'FUNCTION Complete',0,stepCt,'Done');
	RETURN 0;

	---Cleanup OVERALL JOB if this proc is being run standalone
	IF newJobFlag = 1
		THEN
		perform tm_cz.cz_end_audit (jobID, 'SUCCESS');
	END IF;
EXCEPTION
	WHEN OTHERS THEN
	errorNumber := SQLSTATE;
		errorMessage := SQLERRM;
		perform tm_cz.cz_error_handler(jobID, procedureName, errorNumber, errorMessage);
		perform tm_cz.cz_end_audit (jobID, 'FAIL');
		RETURN -16;
END;
$body$
LANGUAGE PLPGSQL;
