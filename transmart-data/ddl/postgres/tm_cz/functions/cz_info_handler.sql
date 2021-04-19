--
-- Name: cz_info_handler(numeric, numeric, numeric, character varying, character varying, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.cz_info_handler(jobid numeric, messageid numeric, messageline numeric, messageprocedure character varying, infomessage character varying, stepnumber character varying) RETURNS numeric
    LANGUAGE plpgsql
AS $$
    declare

    databaseName VARCHAR(100);

begin

    select database_name INTO databaseName
      from tm_cz.cz_job_master
     where job_id=jobID;

    perform tm_cz.cz_write_audit( jobID, databaseName, messageProcedure, 'Step contains more details', 0, stepNumber, 'Information' );
    perform tm_cz.cz_write_info(jobID, messageID, messageLine, messageProcedure, infoMessage );

    return 1;

end;
$$;

