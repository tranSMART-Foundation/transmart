--
-- Name: util_grant_all(character varying, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.util_grant_all(username character varying DEFAULT 'DATATRUST'::character varying, v_whattype character varying DEFAULT 'PROCEDURES,FUNCTIONS,TABLES,VIEWS,PACKAGES'::character varying) RETURNS void
    LANGUAGE plpgsql
AS $$
    declare

    -------------------------------------------------------------------------------------
    -- NAME: UTIL_GRANT_ALL
    --
    -- Copyright c 2011 Recombinant Data Corp.
    --

    --------------------------------------------------------------------------------------

    --GRANTS DATATRUST POSSIBLE PERMISSIONS
    --ON OBJECTS OWNED BY THE CURRENT USER

    --	JEA@20110901	Added parameter to allow username other than DATATRUST, look for EXTRNL as external table names

    v_user      text2(2000) := SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA');

begin

    if upper(v_whattype) like '%TABLE%' then
	raise notice '%%%%', 'Owner ' ,  v_user  ,  '   Grantee ' ,  username;
	raise notice 'Tables';

	for L_TABLE in (select table_name
			  from user_tables
			 where table_name not like '%EXTRNL%') loop

	    if L_TABLE.table_name like '%EXTRNL%' then
		--grant select only to External tables
		execute 'grant select on ' || L_TABLE.table_name || ' to ' || username;

	    else
		--Grant full permissions on regular tables
		execute 'grant select, insert, update, delete on ' || L_TABLE.table_name || ' to ' || username;
		--raise debug 'grant select, insert, update, delete on ' || L_TABLE.table_name || ' to ' || username;
	    end if;

	end loop; --TABLE LOOP
    end if;

    if upper(v_whattype) like '%VIEW%' then
	raise notice '%%%%', 'Owner ' ,  v_user  ,  '   Grantee ' ,  username;
	raise notice 'Views';

	for L_VIEW in (select view_name
			 from user_views ) loop
            execute 'grant select on ' || L_VIEW.view_name || ' to ' || username;

	end loop; --table loop
    end if;

    if upper(v_whattype) like '%FUNCTION%' or upper(v_whattype) like '%FUNCTION%' or upper(v_whattype) like '%PACKAGE%' then
	raise notice '%%', chr(10) ,  'Procedures, functions and packages';

	for L_PROCEDURE in (select object_name from user_objects where object_type in ('FUNCTION', 'FUNCTION', 'PACKAGE') )
	    loop

	    execute 'grant execute on ' || L_PROCEDURE.object_name || ' to ' || username;
	    -- raise debug 'grant execute on ' || L_PROCEDURE.object_name || ' to ' || username;

	end loop; --procedure loop
    end if;

end;

$$;

