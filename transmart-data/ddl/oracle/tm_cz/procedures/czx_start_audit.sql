--
-- Type: PROCEDURE; Owner: TM_CZ; Name: CZX_START_AUDIT
--
CREATE OR REPLACE PROCEDURE TM_CZ.CZX_START_AUDIT (
    V_JOB_NAME IN VARCHAR2 DEFAULT NULL ,
    V_DATABASE_NAME IN VARCHAR2 DEFAULT NULL ,
    O_JOB_ID OUT NUMBER
)
    AUTHID CURRENT_USER
as
    PRAGMA AUTONOMOUS_TRANSACTION;

    /*************************************************************************
     * Copyright 2008-2012 Janssen Research and Development, LLC.
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

    v_os_user	varchar2(200);
    v_job_id	number;

BEGIN

    insert into tm_cz.cz_job_master (
	start_date,
	active,
	database_name,
	job_name,
	job_status )
    values (
	sysdate,
	'y',
	v_database_name,
	v_job_name,
	'Running' )

	   returning job_id into v_job_id;

    select sys_context('USERENV','OS_USER') into v_os_user from dual;

    insert into tm_cz.cz_job_message ( job_id, message_id, message_procedure, info_message )
    values ( v_job_id, 1, 'OS user name',v_os_user );

    commit;

    o_job_id := v_job_id;

EXCEPTION
    WHEN OTHERS THEN ROLLBACK;
END;
/

