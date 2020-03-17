--
-- Name: util_is_populated(character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.util_is_populated(tabname character varying, OUT retval integer) RETURNS integer
    LANGUAGE plpgsql
AS $$
    declare

    -------------------------------------------------------------------------------------
    -- NAME: UTIL_IS_POPULATED
    --
    -- Copyright c 2011 Recombinant Data Corp.
    --

    --------------------------------------------------------------------------------------
    sqltext varchar(500);
    l_count integer;


begin

    sqltext := 'select count(*) into result from ' || tabname;

    execute sqltext into l_count;


    if l_count > 0 then
	retval :=1;
    else
	retval := 0;
    end if;

    --dbms_output.put_line(l_count);

end;

$$;

