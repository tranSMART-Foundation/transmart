--
-- Name: add_platform(character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.add_platform(pfmName character varying, pfmVersion character varying, pfmDescription character varying,
       pfmArray character varying, pfmAccession character varying, pfmOrganism character varying,
       pfmVendor character varying, pfmType character varying, pfmTechnology character varying) RETURNS integer
    LANGUAGE plpgsql SECURITY DEFINER
AS $$

    /*************************************************************************
     * Copyright 2019 Oryza Bioinformatcs Ltd
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
     **************************************************************************/

    declare

    pExists		integer;
    bapId		character varying;
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
    newId               integer;

begin

    stepCt := 0;

    --Set Audit Parameters
    newJobFlag := 1;
    select tm_cz.cz_start_audit (procedureName, databaseName) into jobId;

    databaseName := 'tm_cz';
    procedureName := 'add_platform';

    -- check pfmOrganism exists in bio_assay_platform
    select count(*) from biomart.bio_assay_platform where platform_organism = pfmOrganism into rowCt;
    stepCt := stepCt + 1;
    if rowCt > 0 then
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'platform organism found in bio_assay_platform',rowCt,stepCt,'Done');
    else
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'platform organism not found in bio_assay_platform',rowCt,stepCt,'Warning');
    end if;

    -- check pfmVendor is known
    select count(*) from biomart.bio_assay_platform where platform_vendor = pfmVendor into rowCt;
    stepCt := stepCt + 1;
    if rowCt > 0 then
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'platform vendor found in bio_assay_platform',rowCt,stepCt,'Done');
    else
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'platform vendor not found in bio_assay_platform',rowCt,stepCt,'Warning');
    end if;

    -- check pfmType is known
    select count(*) from biomart.bio_assay_platform where platform_type = pfmType into rowCt;
    stepCt := stepCt + 1;
    if rowCt > 0 then
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'platform type found in bio_assay_platform',rowCt,stepCt,'Done');
    else
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'platform type not found in bio_assay_platform',rowCt,stepCt,'Warning');
    end if;

    -- check pfmTechnology is known
    select count(*) from biomart.bio_assay_platform where platform_technology = pfmTechnology into rowCt;
    stepCt := stepCt + 1;
    if rowCt > 0 then
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'platform technology found in bio_assay_platform',rowCt,stepCt,'Done');
    else
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'platform technology not found in bio_assay_platform',rowCt,stepCt,'Warning');
    end if;

    -- check whether pfmAccession already exists

    select count(*) from biomart.bio_assay_platform where platform_accession = pfmAccession into rowCt;
    if rowCt > 0 then
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'platform accession already exists in bio_assay_platform',rowCt,stepCt,'Done');
	perform tm_cz.cz_end_audit (jobId, 'WARNING');
--	return -1;
    end if;
    select bio_data_id from biomart.bio_data_uid where unique_id = bapId into newId;
    if newId > 0 then
	stepCt := stepCt + 1;
	msgTxt := 'platform accession ' || pfmAccession || ' already exists in bio_data_uid with ID ' || newId;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,msgTxt,1,stepCt,'Done');
	perform tm_cz.cz_end_audit (jobId, 'WARNING');
	return -1;
    end if;

    insert into biomart.bio_assay_platform (
	platform_name
	,platform_version
	,platform_description
	,platform_array
	,platform_accession
	,platform_organism
	,platform_vendor
	,platform_type
	,platform_technology)
    values (
	pfmName
	,pfmVersion
	,pfmDescription
	,pfmArray
	,pfmAccession
	,pfmOrganism
	,pfmVendor
	,pfmType
	,pfmTechnology
    );

    select bio_assay_platform_id from biomart.bio_assay_platform where platform_accession = pfmAccession into newId;
    stepCt := stepCt + 1;
    msgTxt := 'platform added with bio_assay_platform_id ' || newId;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,msgTxt,1,stepCt,'Done');

    bapId := 'BAP:' || pfmAccession;

    insert into biomart.bio_data_uid (bio_data_id, unique_id, bio_data_type) values (newId, bapId, 'BIO_ASSAY_PLATFORM');
    stepCt := stepCt + 1;
    msgTxt := 'bio_assay_platform added to bio_data_uid ' || newId;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,msgTxt,1,stepCt,'Done');


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

