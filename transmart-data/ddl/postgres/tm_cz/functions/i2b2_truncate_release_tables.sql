--
-- Name: i2b2_truncate_release_tables(); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_truncate_release_tables() RETURNS void
    LANGUAGE plpgsql
AS $$
    declare

    --	Procedure to run one test in CZ_TEST

    --	JEA@20111019	New

    --	Define the abstract result set record

    tabSize integer;
    tabList character varying(500)[] = array(select tablename from pg_tables where tableowner = 'tm_cz' and tablename like '%_release');

    --	Variables

    tText 			varchar(2000);

    --Audit variables
    newJobFlag numeric(1);
    databaseName varchar(100);
    procedureName varchar(100);
    jobID integer;
    stepCt integer;


begin

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := -1;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_truncate_release_tables';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(coalesce(jobID::text, '') = '' or jobID < 1) then
	newJobFlag := 1; -- True
	perform tm_cz.cz_start_audit(procedureName, databaseName, jobID);
    end if;

    stepCt := 0;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Starting i2b2_truncate_release_tables',0,stepCt,'Done');
    stepCt := stepCt + 1;

    tabSize = array_length(tabList, 1);

    for i in 0 .. (tabSize - 1) loop
	raise notice '%', tabList[i];

	if (tabList[i] IS NOT NULL AND tablist[i] <> '') then
	    tText := 'truncate table ' || tabList[i];

	    execute(tText);
	    tText := 'Truncated ' || tabList[i];
	    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,tText,0,stepCt,'Done');

	end if;

    end loop;

    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'End i2b2_truncate_release_tables',tabSize,stepCt,'Done');
    stepCt := stepCt + 1;

end;

$$;

