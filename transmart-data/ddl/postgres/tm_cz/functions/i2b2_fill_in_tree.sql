--
-- Name: i2b2_fill_in_tree(character varying, character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_fill_in_tree(trial_id character varying, path character varying, currentjobid numeric DEFAULT 0) RETURNS numeric
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

    --Audit variables
    newJobFlag		integer;
    databaseName 	VARCHAR(100);
    procedureName 	VARCHAR(100);
    jobID 			numeric(18,0);
    stepCt 			numeric(18,0);
    rowCt			numeric(18,0);
    errorNumber		character varying;
    errorMessage	character varying;

    TrialID varchar(100);
    auditText varchar(4000);
    root_node varchar(1000);
    node_name varchar(1000);
    v_count numeric;
    rtnCd   integer;

    --Get the nodes
    --Trimming off the last node as it would never need to be added.
    cNodes cursor is
	       select distinct substr(c_fullname, 1,tm_cz.instr(c_fullname,'\',-2,1)) as c_fullname
	       from i2b2metadata.i2b2
	       where c_fullname like path || '%' escape '`';

begin
    TrialID := upper(trial_id);

    stepCt := 0;
    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_fill_in_tree';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(jobID IS NULL or jobID < 1) then
	newJobFlag := 1; -- True
	select tm_cz.cz_start_audit (procedureName, databaseName) into jobID;
    end if;

    --start node with the first slash

    --Iterate through each node
    for r_cNodes in cNodes loop
	root_node := '\';
	--Determine how many nodes there are
	--Iterate through, Start with 2 as one will be null from the parser

	for loop_counter in 2 .. (length(r_cNodes.c_fullname) - coalesce(length(replace(r_cNodes.c_fullname, '\','')),0)) / length('\')
	    loop
	    --Determine Node:
	    node_name := tm_cz.parse_nth_value(r_cNodes.c_fullname, loop_counter, '\');
	    root_node :=  root_node || node_name || '\';

            --Check if node exists. If it does not, add it.
            select count(*) into v_count
              from i2b2metadata.i2b2
             where c_fullname = root_node;

            --If it doesn't exist, add it
            if v_count = 0 then
		auditText := 'Inserting ' || root_node;
		stepCt := stepCt + 1;
		perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,auditText,0,stepCt,'Done');
		select tm_cz.i2b2_add_node(trial_id, root_node, node_name, jobId) into rtnCd;
		if(rtnCd <> 1) then
		    stepCt := stepCt + 1;
                    auditText := 'Failed to add leaf node '|| root_node;
	            perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,auditText,0,stepCt,'Message');
	            perform tm_cz.cz_end_audit (jobID, 'FAIL');
	            return -16;
                end if;
            end if;

	end loop;

	--reset variables
	root_node := '';
	node_name := '';
    end loop;

    ---Cleanup OVERALL JOB if this proc is being run standalone
    if newJobFlag = 1 then
	perform tm_cz.cz_end_audit (jobID, 'SUCCESS');
    end if;

    return 1;

exception
    when others then
	errorNumber := SQLSTATE;
	errorMessage := SQLERRM;
    --Handle errors.
	perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
    --End Proc
	perform tm_cz.cz_end_audit (jobID, 'FAIL');
	return -16;


end;

$$;

