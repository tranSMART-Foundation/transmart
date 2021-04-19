--
-- Name: cz_write_info(numeric, numeric, numeric, character varying, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.cz_write_info(jobid numeric, messageid numeric, messageline numeric, messageprocedure character varying, infomessage character varying) RETURNS numeric
    LANGUAGE plpgsql SECURITY DEFINER
AS $$

    declare

    debugValue	character varying(255);

begin
    select paramvalue
      into debugValue
      from tm_cz.etl_settings
     where paramname in ('debug','DEBUG');

    begin
	insert into tm_cz.cz_job_message
		    (job_id
		    ,message_id
		    ,message_line
		    ,message_procedure
		    ,info_message
		    ,seq_id)
	select
	    jobID
	    ,messageID
	    ,messageLine
	    ,messageProcedure
	    ,infoMessage
	    ,max(seq_id)
	  from tm_cz.cz_job_audit
	 where job_id = jobID;
    end;

    if (coalesce(debugValue,'no')) then
        raise notice 'CZ_WRITE_INFO job:% id:% line:% procedure:% info:%',
	      jobId, messageID, messageLine, messageProcedure, infoMessage;
    commit;
    return 1;

end;

$$;

