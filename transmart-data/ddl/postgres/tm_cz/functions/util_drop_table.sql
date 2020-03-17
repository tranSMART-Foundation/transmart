--
-- Name: util_drop_table(character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.util_drop_table(v_tabname character varying DEFAULT NULL::character varying) RETURNS void
    LANGUAGE plpgsql
AS $$

    -- Attention:
    -- Oracle procedure
    -- can postgres check for user tables (oracle had a user_tables directory)

    declare

    -------------------------------------------------------------------------------------
    -- NAME: UTIL_DROP_TABLE
    --
    -- Copyright c 2011 Recombinant Data Corp.
    --

    --------------------------------------------------------------------------------------
    v_exists integer;
    v_cmdline varchar(200);


begin

    --Check if table exists
    select count(*)
      into v_exists
      from pg_tables
     where tablename = v_tabname;

    if v_exists > 0 then
	v_cmdline := 'drop table ' || v_tabname;
	execute v_cmdline;
    end if;

end;

$$;

