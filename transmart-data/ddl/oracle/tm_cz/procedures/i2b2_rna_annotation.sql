--
-- Type: PROCEDURE; Owner: TM_CZ; Name: I2B2_RNA_ANNOTATION
--
CREATE OR REPLACE PROCEDURE TM_CZ.I2B2_RNA_ANNOTATION (
    rtn_code OUT NUMBER,
    currentJobID NUMBER := null
)

AS

    gpl_rtn NUMBER;
    missing_platform	exception;
    newJobFlag INTEGER(1);
    databaseName VARCHAR(100);
    procedureName VARCHAR(100);
    jobID number(18,0);
    stepCt number(18,0);

BEGIN

    -- insert into "DEAPP"."DE_RNA_ANNOTATION"
    stepCt := 0;

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    SELECT sys_context('USERENV', 'CURRENT_SCHEMA') INTO databaseName FROM dual;
    procedureName := $$PLSQL_UNIT;

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    IF(jobID IS NULL or jobID < 1) THEN
	newJobFlag := 1; -- True
	tm_cz.cz_start_audit (procedureName, databaseName, jobID);
    END IF;

    stepCt := stepCt + 1;
    tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Starting i2b2_rna_annotation',0,stepCt,'Done');

    insert into deapp.de_rna_annotation (
	TRANSCRIPT_ID
	,GPL_ID
	,GENE_SYMBOL
	,GENE_ID
	,ORGANISM
	-- ,PROBESET_ID
    )
    select
	distinct (a.transcript_id)
	--,g.platform
	,null
        ,a.gene_symbol
        ,null--b.primary_external_id
        ,a.organism
        -- ,null
        --,pd.probeset_id
      from tm_lz.lt_rna_annotation a
        --,(select platform from deapp.de_gpl_info where marker_type='RNASEQ') g
        -- ,biomart.bio_marker b
        --  ,tm_cz.probeset_deapp pd
     where ---b.bio_marker_name=a.gene_symbol
           --and a.transcript_id =pd.probeset
           --  and
         a.transcript_id not in (select distinct transcript_id from deapp.de_rna_annotation);

    stepCt := stepCt + 1;
    tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert data in DE_RNA_ANNOTATION',0,stepCt,'Done');

    ---update gene_id from biomart.bio_marker  table
    update deapp.de_rna_annotation a
       set GENE_ID=(select primary_external_id
		      from biomart.bio_marker b
		     where
			 b.bio_marker_name=a.gene_symbol and rownum=1)
     where a.GENE_ID is null;

    stepCt := stepCt + 1;
    tm_cz.cz_write_audit(jobId,databaseName,procedureName,'End i2b2_rna_annotation',0,stepCt,'Done');

    IF newJobFlag = 1 THEN
	tm_cz.cz_end_audit (jobID, 'SUCCESS');
    END IF;

    select 0 into rtn_code from dual;

    --update deapp.de_rna_annotation
    --     update deapp.de_rna_annotation  set gene_id=
    --      ---Exceptions occur
    --
EXCEPTION
    WHEN OTHERS THEN
    --Handle errors.
    --tm_cz.cz_error_handler (jobID, procedureName);
    --End Proc
    --tm_cz.cz_end_audit (jobID, 'FAIL');
	tm_cz.cz_error_handler (jobID, procedureName);
    --End Proc
	tm_cz.cz_end_audit (jobID, 'FAIL');

	select 162  into rtn_code from dual;

END ;
/

