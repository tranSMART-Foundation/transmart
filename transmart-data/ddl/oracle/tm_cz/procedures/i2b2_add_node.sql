--
-- Type: PROCEDURE; Owner: TM_CZ; Name: I2B2_ADD_NODE
--
CREATE OR REPLACE PROCEDURE TM_CZ.I2B2_ADD_NODE (
    TrialID VARCHAR2,
    path VARCHAR2,
    path_name VARCHAR2
    ,currentJobID NUMBER := null
)
AS
    /*************************************************************************
     * Copyright 2008-2012 Janssen Research and Development, LLC.
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

    root_node		varchar2(2000);
    root_level		int;
    tText		varchar2(2000);

    --Audit variables
    newJobFlag		INTEGER(1);
    databaseName 	VARCHAR(100);
    procedureName 	VARCHAR(100);
    jobID 		number(18,0);
    stepCt 		number(18,0);

BEGIN

    stepCt := 0;

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    SELECT sys_context('USERENV', 'CURRENT_SCHEMA') INTO databaseName FROM dual;
    procedureName := $$PLSQL_UNIT;

    select tm_cz.parse_nth_value(path, 2, '\') into root_node from dual;

    select c_hlevel into root_level
      from i2b2metadata.table_access
     where c_name = root_node;

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    IF(jobID IS NULL or jobID < 1) THEN
	newJobFlag := 1; -- True
	tm_cz.cz_start_audit (procedureName, databaseName, jobID);
    END IF;

    if path = ''  or path = '%' or path_name = '' then
  	stepCt := stepCt + 1;
	tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Missing path or name - path:' || path || ' name: ' || path_name,SQL%ROWCOUNT,stepCt,'Done');
    else
	stepCt := stepCt + 1;
	tText := 'Add path ' || path;
	tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,SQL%ROWCOUNT,stepCt,'Done');
	--Delete existing node.
	--I2B2
	DELETE FROM i2b2demodata.observation_fact
	 WHERE concept_cd IN (
	     SELECT C_BASECODE FROM I2B2 WHERE C_FULLNAME = PATH);
	stepCt := stepCt + 1;
	tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Deleted any concepts for path from I2B2DEMODATA observation_fact',SQL%ROWCOUNT,stepCt,'Done');
	COMMIT;

	--CONCEPT DIMENSION
	delete from i2b2demodata.concept_dimension
	 where concept_path = path;
	stepCt := stepCt + 1;
	tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Deleted any concepts for path from I2B2DEMODATA concept_dimension',SQL%ROWCOUNT,stepCt,'Done');
	COMMIT;

	--I2B2
	delete from i2b2metadata.i2b2
	 where c_fullname = path;
	stepCt := stepCt + 1;
	tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Deleted path from I2B2METADATA i2b2',SQL%ROWCOUNT,stepCt,'Done');
	COMMIT;

	--CONCEPT DIMENSION
	insert into i2b2demodata.concept_dimension (
	    CONCEPT_CD
	    , CONCEPT_PATH
	    , NAME_CHAR
	    ,  UPDATE_DATE
	    ,  DOWNLOAD_DATE
	    , IMPORT_DATE
	    , SOURCESYSTEM_CD)
	VALUES (
	    concept_id.nextval,
	    path,
	    to_char(path_name),
	    sysdate,
	    sysdate,
	    sysdate,
	    TrialID);
	stepCt := stepCt + 1;
	tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Inserted concept for path into I2B2DEMODATA concept_dimension',SQL%ROWCOUNT,stepCt,'Done');
	COMMIT;

	--I2B2
	insert into i2b2metadata.i2b2 (
	    c_hlevel
	    , C_FULLNAME
	    , C_NAME
	    , C_VISUALATTRIBUTES
	    , c_synonym_cd
	    , C_FACTTABLECOLUMN
	    , C_TABLENAME
	    , C_COLUMNNAME
	    , C_DIMCODE
	    , C_TOOLTIP
	    , UPDATE_DATE
	    , DOWNLOAD_DATE
	    , IMPORT_DATE
	    , SOURCESYSTEM_CD
	    , c_basecode
	    , C_OPERATOR
	    , c_columndatatype
	    , c_comment
	    , m_applied_path)
	SELECT (
	    length(concept_path) - nvl(length(replace(concept_path, '\')),0)) / length('\') - 2 + root_level
	       ,CONCEPT_PATH
	       ,NAME_CHAR
	       ,'FA'
	       ,'N'
	       ,'CONCEPT_CD'
	       ,'CONCEPT_DIMENSION'
	       ,'CONCEPT_PATH'
	       ,CONCEPT_PATH
	       ,CONCEPT_PATH
	       ,sysdate
	       ,sysdate
	       ,sysdate
	       ,SOURCESYSTEM_CD
	       ,CONCEPT_CD
	       ,'LIKE'
	       ,'T'
	       ,decode(TrialID,null,null,'trial:' || TrialID)
	       ,'@'
	  from i2b2demodata.concept_dimension
	 where CONCEPT_PATH = path;
	stepCt := stepCt + 1;
	tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Inserted path into I2B2METADATA i2b2',SQL%ROWCOUNT,stepCt,'Done');
	COMMIT;
    END IF;
    ---Cleanup OVERALL JOB if this proc is being run standalone
    IF newJobFlag = 1 THEN
	tm_cz.cz_end_audit (jobID, 'SUCCESS');
    END IF;

EXCEPTION
    WHEN OTHERS THEN
    --Handle errors.
	tm_cz.cz_error_handler (jobID, procedureName);
    --End Proc
	tm_cz.cz_end_audit (jobID, 'FAIL');
END;
/

