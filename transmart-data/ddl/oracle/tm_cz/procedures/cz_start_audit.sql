--
-- Type: PROCEDURE; Owner: TM_CZ; Name: CZ_START_AUDIT
--
CREATE OR REPLACE PROCEDURE TM_CZ.CZ_START_AUDIT (
    jobName IN VARCHAR2
    ,databaseName IN VARCHAR2
    ,jobID OUT NUMBER
)
AS

BEGIN

    insert into tm_cz.cz_job_master (
	start_date
	,active
	--,username
	--,session_id
	,database_name
	,job_name
	,job_status)
    VALUES(
	SYSTIMESTAMP
	,'Y'
	--,suser_name()
	--,@@SPID
	,databaseName
	,jobName
	,'Running')
	RETURNING job_id INTO jobID;

    COMMIT;

END;
/
