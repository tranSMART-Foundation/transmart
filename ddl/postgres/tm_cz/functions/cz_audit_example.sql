--
-- Name: cz_audit_example(bigint); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION cz_audit_example(currentjobid bigint) RETURNS void
    LANGUAGE plpgsql
    AS $_$
DECLARE

  --Audit variables
  newJobFlag numeric(1);
  databaseName varchar(100);
  procedureName varchar(100);
  jobID bigint;
  rowCt integer;
  rtnCd integer;

BEGIN
  --Set Audit Parameters
  newJobFlag := 0; -- False (Default)
  jobID := currentJobID;

  PERFORM sys_context('USERENV', 'CURRENT_SCHEMA') INTO databaseName ;
  procedureName := 'CZ_AUDIT_EXAMPLE';

  --Audit JOB Initialization
  --If Job ID does not exist, then this is a single procedure run and we need to create it
  IF(coalesce(jobID::text, '') = '' or jobID < 1)
  THEN
    newJobFlag := 1; -- True
    RAISE NOTICE '%', 'Here' || to_char(jobID);
    select cz_start_audit (procedureName, databaseName, jobID) into rtnCd;
    RAISE NOTICE '%', 'Here2' || to_char(jobID);
  END IF;

  --Step Audit
  rowCt := 0;
  select cz_write_audit (jobID, databaseName, procedureName, 'Start loading some data', rowCt, 1, 'PASS') into rtnCd;

  begin
	update cz_job_master set job_name = job_name;
	get diagnostics rowCt := ROW_COUNT;
  end;

  --Step Audit
  select cz_write_audit (jobID, databaseName, procedureName, '# of rows on the cz_job_master table', rowCt, 2, 'PASS') into rtnCd;


  select cz_write_info (jobID, 1, 39, procedureName, 'Writing a message') into rtnCd;



  --invalid statement
  begin
      insert into az_test_run(dw_version_id)
      values('a');
      get diagnostics rowCt := ROW_COUNT;
  end;

  --Step Audit
  select cz_write_audit (jobID, databaseName, procedureName, 'Should have caused an error!', rowCt, 3, 'PASS') into rtnCd;


  ---Cleanup OVERALL JOB if this proc is being run standalone
  IF newJobFlag = 1
  THEN
    select cz_end_audit (jobID, 'SUCCESS') into rtnCd;
  END IF;

  EXCEPTION
  WHEN OTHERS THEN
    --Handle errors.
    select cz_error_handler(jobId, procedureName, SQLSTATE, SQLERRM) into rtnCd;
    --End Proc
    select cz_end_audit (jobID, 'FAIL') into rtnCd;

END;
 
$_$;

