--
-- Name: i2b2_load_security_data(numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_load_security_data(currentjobid numeric DEFAULT 0) RETURNS numeric
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

    --Audit variables
    databaseName 	VARCHAR(100);
    procedureName 	VARCHAR(100);
    jobID 			numeric(18,0);
    stepCt 			numeric(18,0);
    rowCt			numeric(18,0);
    errorNumber		character varying;
    errorMessage	character varying;

begin

    --Set Audit Parameters
    databaseName := 'tm_cz';
    procedureName := 'i2b2_load_security_data';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    select case when coalesce(currentjobid, -1) < 1 then tm_cz.cz_start_audit(procedureName, databaseName) else currentjobid end into jobId;

    truncate table i2b2metadata.i2b2_secure;

    stepCt := 0;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Truncate I2B2METADATA i2b2_secure',0,stepCt,'Done');

    insert into i2b2metadata.i2b2_secure
		(c_hlevel,
		c_fullname,
		c_name,
		c_synonym_cd,
		c_visualattributes,
		c_totalnum,
		c_basecode,
		c_metadataxml,
		c_facttablecolumn,
		c_tablename,
		c_columnname,
		c_columndatatype,
		c_operator,
		c_dimcode,
		c_comment,
		c_tooltip,
		update_date,
		download_date,
		import_date,
		sourcesystem_cd,
		valuetype_cd,
		secure_obj_token)
    select
	b.c_hlevel,
	b.c_fullname,
	b.c_name,
	b.c_synonym_cd,
	b.c_visualattributes,
	b.c_totalnum,
	b.c_basecode,
	b.c_metadataxml,
	b.c_facttablecolumn,
	b.c_tablename,
	b.c_columnname,
	b.c_columndatatype,
	b.c_operator,
	b.c_dimcode,
	b.c_comment,
	b.c_tooltip,
	b.update_date,
	b.download_date,
	b.import_date,
	b.sourcesystem_cd,
	b.valuetype_cd,
	coalesce(f.tval_char,'EXP:PUBLIC')
      from i2b2metadata.i2b2 b
	       left outer join (select distinct
				    modifier_cd
				    ,tval_char
				  from i2b2demodata.observation_fact
				 where concept_cd = 'SECURITY') f
				  on b.sourcesystem_cd = f.modifier_cd;
    get diagnostics rowCt := ROW_COUNT;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Reload security data into I2B2METADATA i2b2_secure',rowCt,stepCt,'Done');

    ---Cleanup OVERALL JOB if this proc is being run standalone
    perform tm_cz.cz_end_audit (jobID, 'SUCCESS') where coalesce(currentJobId, -1) <> jobId;

    return 1;

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

$$;

--
-- Name: i2b2_load_security_data(character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_load_security_data(sourcesystemcd character varying, currentjobid numeric DEFAULT 0) RETURNS numeric
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

    --Audit variables
    databaseName 	VARCHAR(100);
    procedureName 	VARCHAR(100);
    jobID 			numeric(18,0);
    stepCt 			numeric(18,0);
    rowCt			numeric(18,0);
    errorNumber		character varying;
    errorMessage	character varying;

begin
    --Set Audit Parameters
    databaseName := 'tm_cz';
    procedureName := 'i2b2_load_security_data';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    select case when coalesce(currentjobid, -1) < 1 then tm_cz.cz_start_audit(procedureName, databaseName) else currentjobid end into jobId;

    delete from i2b2metadata.i2b2_secure where sourcesystem_cd = sourcesystemCd;

    get diagnostics rowCt := ROW_COUNT;
    stepCt := 0;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Clean-up I2B2METADATA i2b2_secure for '||sourcesystemcd,rowCt,stepCt,'Done');

    insert into i2b2metadata.i2b2_secure
		(c_hlevel,
		c_fullname,
		c_name,
		c_synonym_cd,
		c_visualattributes,
		c_totalnum,
		c_basecode,
		c_metadataxml,
		c_facttablecolumn,
		c_tablename,
		c_columnname,
		c_columndatatype,
		c_operator,
		c_dimcode,
		c_comment,
		c_tooltip,
		update_date,
		download_date,
		import_date,
		sourcesystem_cd,
		valuetype_cd,
		secure_obj_token)
    select
	b.c_hlevel,
	b.c_fullname,
	b.c_name,
	b.c_synonym_cd,
	b.c_visualattributes,
	b.c_totalnum,
	b.c_basecode,
	b.c_metadataxml,
	b.c_facttablecolumn,
	b.c_tablename,
	b.c_columnname,
	b.c_columndatatype,
	b.c_operator,
	b.c_dimcode,
	b.c_comment,
	b.c_tooltip,
	b.update_date,
	b.download_date,
	b.import_date,
	b.sourcesystem_cd,
	b.valuetype_cd,
	coalesce(f.tval_char,'EXP:PUBLIC')
      from i2b2metadata.i2b2 b
	       left outer join (select distinct
				    sourcesystem_cd
				    ,tval_char
				  from i2b2demodata.observation_fact
				 where concept_cd = 'SECURITY') f
				  on b.sourcesystem_cd = f.sourcesystem_cd
     where b.sourcesystem_cd = sourcesystemCd;

    get diagnostics rowCt := ROW_COUNT;
    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert security data into I2B2METADATA i2b2_secure for '||sourcesystemcd,rowCt,stepCt,'Done');

    ---Cleanup OVERALL JOB if this proc is being run standalone
    perform tm_cz.cz_end_audit (jobID, 'SUCCESS') where coalesce(currentJobId, -1) <> jobId;

    return 1;

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

$$;

