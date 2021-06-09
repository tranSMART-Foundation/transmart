--
-- Name: i2b2_move_node(character varying, character varying, character varying, numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_move_node(old_path character varying, new_path character varying, topnode character varying, currentjobid numeric DEFAULT 0) RETURNS void
    LANGUAGE plpgsql
AS $$
    declare

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

    root_node	varchar(2000);
    root_level	integer;

    --Audit variables
    newJobFlag numeric(1);
    databaseName varchar(100);
    procedureName varchar(100);
    jobID numeric;
    stepCt integer;


begin

    stepCt := 0;

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    perform sys_context('userenv', 'current_schema') INTO databaseName ;
    procedureName := 'i2b2_move_node';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(coalesce(jobID::text, '') = '' or jobID < 1) then
	newJobFlag := 1; -- True
	perform tm_cz.cz_start_audit (procedureName, databaseName, jobID);
    end if;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Start i2b2_move_node',0,stepCt,'Done');

    perform tm_cz.parse_nth_value(topNode, 2, '\') into root_node ;

    select c_hlevel into root_level
      from i2b2metadata.table_access
     where c_name = root_node;

    if old_path != ''  or old_path != '%' or new_path != ''  or new_path != '%'
    then
	--concept dimension
	update i2b2demodata.concept_dimension
	set concept_path = replace(concept_path, old_path, new_path)
	where concept_path like old_path || '%';
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update concept_dimension with new path',SQL%ROWCOUNT,stepCt,'Done');
	commit;

	--i2b2
	update i2b2metadata.i2b2
	   set c_fullname = replace(c_fullname, old_path, new_path)
	 where c_fullname like old_path || '%';
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update i2b2 with new path',SQL%ROWCOUNT,stepCt,'Done');
	commit;

	--update level data
	update i2b2metadata.i2b2
	   set c_hlevel = (length(c_fullname) - coalesce(length(replace(c_fullname, '\')),0)) / length('\') - 2 + root_level
	 where c_fullname like new_path || '%';
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update i2b2 with new level',SQL%ROWCOUNT,stepCt,'Done');
	commit;

	--Update tooltip and dimcode
	update i2b2metadata.i2b2
	   set c_dimcode = c_fullname,
	       c_tooltip = c_fullname
	 where c_fullname like new_path || '%';
	stepCt := stepCt + 1;
	perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update i2b2 with new dimcode and tooltip',SQL%ROWCOUNT,stepCt,'Done');
	commit;

	end if;

    if newJobFlag = 1 then
	perform tm_cz.cz_end_audit (jobID, 'SUCCESS');
    end if;

exception
    when others then
    --handle errors.
	perform tm_cz.cz_error_handler(jobId, procedureName, SQLSTATE, SQLERRM);
    --End Proc
	perform tm_cz.cz_end_audit (jobID, 'FAIL');

end;

$$;

