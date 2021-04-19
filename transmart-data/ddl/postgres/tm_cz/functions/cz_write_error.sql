--
-- Name: cz_write_error(numeric, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.cz_write_error(jobId numeric, errorNumber character varying, errorMessage character varying, errorStack text, errorBacktrace text) RETURNS numeric
    LANGUAGE plpgsql SECURITY DEFINER
AS $$
    /*************************************************************************
     * Copyright 2008-2012 Janssen Research & Development, LLC.
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
     ******************************************************************/

    declare
    debugValue	character varying(255);

begin

    begin
        select paramvalue
          into debugValue
          from tm_cz.etl_settings
         where paramname in ('debug','DEBUG');

        insert into tm_cz.cz_job_error(
	    job_id
	    ,error_number
	    ,error_message
	    ,error_stack
	    ,error_backtrace
	    ,seq_id)
	select
	    jobId
	    ,errorNumber
	    ,errorMessage
	    ,errorStack
	    ,errorBackTrace
	    ,max(seq_id)
	  from tm_cz.cz_job_audit
	 where job_id = jobId;

        if (coalesce(debugValue,'no')) then
            if(coalesce(errorBacktrace,'') <> '') then
		errorBacktrace := 'backtrace: "' || errorBacktrace || '"';
	    end if;
	    if(coalesce(errorStack,'') <> '') then
		errorStack := 'stack: "' || errorStack || '"';
	    end if;
	    raise notice 'CZ_WRITE_ERROR job:% error:% "%" % %',
	        jobId, errorNumber, errorMessage, errorStack, errorBacktrace;
        end if;

    end;

    return 1;

exception
    when others then
	raise notice 'cz_write_error failed state=%  errm=%', SQLSTATE, SQLERRM;
	return -16;

end;
$$;

