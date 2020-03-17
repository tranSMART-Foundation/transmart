--
-- Name: util_recompile_all(); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.util_recompile_all() RETURNS void
    LANGUAGE plpgsql
AS $$
    declare

    -- Attention:
    -- Oracle procedure
    -- need postgreSQL version

    -------------------------------------------------------------------------------------
    -- NAME: UTIL_RECOMPILE_ALL
    --
    -- Copyright c 2011 Recombinant Data Corp.
    --

    --------------------------------------------------------------------------------------
    v_proclist cursor for
			  select distinct 'alter '|| object_type || ' ' || object_name || ' compile '
			  from user_procedures;

    v_procname varchar(50);


begin

    open v_proclist;
    fetch v_proclist into v_procname;
    while v_proclist%FOUND
	loop

	begin
            begin

		begin
		    execute v_procname;
		    raise notice '%%', 'succesfully compiled ' ,  v_procname;
		end;
            exception
		when others then

		    begin
			raise notice '%%', 'error compiling ' ,  v_procname;
		    end;
            end;
            fetch v_proclist into v_procname;
	end;
    end loop;
    -- while loop
    close v_proclist;-- procedure

end;

$$;

