--
-- Type: PROCEDURE; Owner: TM_CZ; Name: CZ_WRITE_AUDIT
--
CREATE OR REPLACE PROCEDURE TM_CZ.CZ_WRITE_AUDIT (
    jobId IN NUMBER
    ,databaseName IN VARCHAR2
    ,procedureName IN VARCHAR2
    ,stepDesc IN VARCHAR2
    ,recordsManipulated IN NUMBER
    ,stepNumber IN NUMBER
    ,stepStatus IN VARCHAR2
)

AS

    lastTime timestamp;
    elapsedSecs number;
    debugValue varchar2(255);

BEGIN

    begin
    select paramvalue
      into debugValue
      from tm_cz.etl_settings
     where paramname in ('debug','DEBUG');
     exception
     when NO_DATA_FOUND then
     	  debugValue := 'no';
    end;

    dbms_output.put_line('Anybody there?');
    if (coalesce(lower(debugValue),'no') != 'no') then
	dbms_output.put_line('debugValue TRUE: '||debugValue);
    else
	dbms_output.put_line('debugValue FALSE: '||debugValue);
    end if;

    select max(job_date)
      into lastTime
      from tm_cz.cz_job_audit
     where job_id = jobID;

    elapsedSecs := COALESCE(((EXTRACT (DAY    FROM (SYSTIMESTAMP - lastTime))*24 +
			      EXTRACT (HOUR   FROM (SYSTIMESTAMP - lastTime)))*60 +
			      EXTRACT (MINUTE FROM (SYSTIMESTAMP - lastTime)))*60 +
			      EXTRACT (SECOND FROM (SYSTIMESTAMP - lastTime)), 0);

    insert into tm_cz.cz_job_audit(
	job_id
	,database_name
 	,procedure_name
 	,step_desc
	,records_manipulated
	,step_number
	,step_status
	,job_date
	,time_elapsed_secs
    )
    select
 	jobId
	,substr(databaseName, 1, 50)
	,procedureName
	,stepDesc
	,recordsManipulated
	,stepNumber
	,stepStatus
	,SYSTIMESTAMP
	,COALESCE(
	    EXTRACT (DAY    FROM (SYSTIMESTAMP - lastTime))*24*60*60 +
	    EXTRACT (HOUR   FROM (SYSTIMESTAMP - lastTime))*60*60 +
	    EXTRACT (MINUTE FROM (SYSTIMESTAMP - lastTime))*60 +
	    EXTRACT (SECOND FROM (SYSTIMESTAMP - lastTime))
	    ,0)
      from dual;

    if (coalesce(lower(debugValue),'no') != 'no') then
	dbms_output.put_line('CZ_WRITE_AUDIT job:' || jobId ||
			     ' function:' || databaseName || '.' || procedureName || ' step:' || stepNumber ||
			     ' records:' || recordsManipulated || '  status:' || stepStatus ||
			     '  date:' || SYSTIMESTAMP || '  elapsed:' || elapsedSecs || '        ' || stepDesc);
    end if;

    COMMIT;

END;
/
