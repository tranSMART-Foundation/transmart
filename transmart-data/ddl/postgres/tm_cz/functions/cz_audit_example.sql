--
-- Name: cz_audit_example(bigint); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.cz_audit_example(currentjobid bigint) RETURNS void
    LANGUAGE plpgsql
AS $$
    declare

    --Audit variables
    newJobFlag numeric(1);
    databaseName varchar(100);
    procedureName varchar(100);
    jobID bigint;
    rowCt integer;

begin
    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentjobid;

    perform sys_context('userenv', 'current_schema') INTO databaseName ;
    procedureName := 'cz_audit_example';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(coalesce(jobID::text, '') = '' or jobID < 1) then
	newJobFlag := 1; -- True
	raise notice '%', 'Here' || to_char(jobID);
	perform tm_cz.cz_start_audit (procedureName, databaseName, jobID);
	raise notice '%', 'Here2' || to_char(jobID);
    end if;

    --Step Audit
    rowCt := 0;
    perform tm_cz.cz_write_audit (jobID, databaseName, procedureName, 'Start loading some data', rowCt, 1, 'PASS');

    begin
	update tm_cz.cz_job_master set job_name = job_name;
	get diagnostics rowCt := ROW_COUNT;
    end;

    --Step Audit
    perform tm_cz.cz_write_audit (jobID, databaseName, procedureName, '# of rows on the cz_job_master table', rowCt, 2, 'PASS');


    perform tm_cz.cz_write_info (jobID, 1, 39, procedureName, 'Writing a message');



    --invalid statement
    begin
	insert into az_test_run(dw_version_id)
	values('a');
	get diagnostics rowCt := ROW_COUNT;
    end;

    --Step Audit
    perform tm_cz.cz_write_audit (jobID, databaseName, procedureName, 'Should have caused an error!', rowCt, 3, 'PASS');


    ---Cleanup OVERALL JOB if this proc is being run standalone
    if newJobFlag = 1 then
	perform tm_cz.cz_end_audit (jobID, 'SUCCESS');
    end if;

exception
    when others then
    --Handle errors.
	perform tm_cz.cz_error_handler(jobId, procedureName, SQLSTATE, SQLERRM);
    --End Proc
	perform tm_cz.cz_end_audit (jobID, 'FAIL');

end;

$$;

