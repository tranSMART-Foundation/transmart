--
-- Name: cz_audit_example(bigint); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION cz_audit_example(currentjobid bigint) RETURNS void
    LANGUAGE plpgsql
AS $$
    declare

    --Audit variables
    newJobFlag numeric(1);
    databaseName varchar(100);
    procedureName varchar(100);
    jobID bigint;
    rowCt integer;
    rtnCd integer;

begin
    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentjobid;

    PERFORM sys_context('USERENV', 'CURRENT_SCHEMA') INTO databaseName ;
    procedureName := 'CZ_AUDIT_EXAMPLE';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(coalesce(jobID::text, '') = '' or jobID < 1) then
	newJobFlag := 1; -- True
	raise notice '%', 'Here' || to_char(jobID);
	select cz_start_audit (procedureName, databaseName, jobID) into rtnCd;
	raise notice '%', 'Here2' || to_char(jobID);
    end if;

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
    if newJobFlag = 1 then
	select cz_end_audit (jobID, 'SUCCESS') into rtnCd;
    end if;

exception
    when others then
    --Handle errors.
	select cz_error_handler(jobId, procedureName, SQLSTATE, SQLERRM) into rtnCd;
    --End Proc
	select cz_end_audit (jobID, 'FAIL') into rtnCd;

end;

$$;

