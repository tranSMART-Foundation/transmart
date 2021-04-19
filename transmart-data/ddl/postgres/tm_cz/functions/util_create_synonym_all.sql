--
-- Name: util_create_synonym_all(character varying, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.util_create_synonym_all(v_fromzone character varying DEFAULT NULL::character varying, v_whattype character varying DEFAULT 'FUNCTIONS,TABLES,VIEWS'::character varying) RETURNS void
    LANGUAGE plpgsql
AS $$
    declare

    -------------------------------------------------------------------------------------
    -- NAME: UTIL_CREATE or REPLACE _SYNONYM_ALL
    --
    -- Copyright c 2011 Recombinant Data Corp.
    --

    --------------------------------------------------------------------------------------
    --The name of the table, proc, function or view.
    V_OBJNAME varchar(50);

    --Dynamic SQL line
    V_CMDLINE varchar(200);

    --Table list
    L_TABLE cursor for
		       select table_name
		       from information_schema.tables
		       where table_schema = lower(v_fromzone);
    --View List
    L_VIEW cursor for
		      select table_name
		      from information_schema.views
		      where table_schema = lower(v_fromzone);

    --function list (FUNCTION)
    L_FUNCTION cursor for
			  select distinct routine_name from information_schema.routines
			  where routine_schema = lower(v_fromzone)
			  and routine_name not like 'util%';

begin

    -- Create synonyms for Tables
    if upper(V_WHATTYPE) like '%TABLE%' then

	open L_TABLE;
	fetch L_TABLE into V_OBJNAME;
	while L_TABLE%FOUND loop
	    begin

		V_CMDLINE := 'create or replace synonym ' || V_OBJNAME || ' for ' || UPPER(V_FROMZONE) || '.' || V_OBJNAME ;

		execute V_CMDLINE;
		--DBMS_OUTPUT.PUT_LINE('SUCCESS ' || V_CMDLINE);

		fetch L_TABLE  into V_OBJNAME;

	    exception
		when others then
		    begin
			raise notice '%%', 'ERROR ' ,  V_CMDLINE;
			raise notice '%', SQLERRM;
		    end;
	    end;
	end loop;
	close L_TABLE;
    end if;

    --CREATE or REPLACE  SYNONYMS FOR VIEWS
    if upper(V_WHATTYPE) like '%VIEW%' then

	open L_VIEW;
	fetch L_VIEW into V_OBJNAME;
	while L_VIEW%FOUND loop
	    begin

		V_CMDLINE := 'create or replace synonym ' || V_OBJNAME || ' for ' || UPPER(V_FROMZONE) || '.' || V_OBJNAME ;

		execute V_CMDLINE;
		--DBMS_OUTPUT.PUT_LINE('SUCCESS ' || V_CMDLINE);

		fetch L_VIEW into V_OBJNAME;

	    exception
		when others then
		    begin
			raise notice '%%', 'ERROR ' ,  V_CMDLINE;
			raise notice '%', SQLERRM;
		    end;
	    end;
	end loop;
	close L_VIEW;
    end if;

    -- CREATE or REPLACE  SYNONYMS FOR FUNCTIONS
    if upper(V_WHATTYPE) like '%FUNCTION%' then

	open l_function('FUNCTION');
	fetch l_function into V_OBJNAME;
	while l_function%FOUND loop
	    begin

		V_CMDLINE := 'create synonym ' || V_OBJNAME || ' for ' || UPPER(V_FROMZONE) || '.' || V_OBJNAME ;

		execute V_CMDLINE;
		--DBMS_OUTPUT.PUT_LINE('SUCCESS ' || V_CMDLINE);

		fetch L_FUNCTION into V_OBJNAME;

	    exception
		when others then
		    begin
			raise notice '%%', 'ERROR ' ,  V_CMDLINE;
			raise notice '%', SQLERRM;
		    end;
	    end;
	end loop;
	close L_FUNCTION;
    end if;
end;

$$;

