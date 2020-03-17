--
-- Name: util_drop_anything(character varying, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.util_drop_anything(v_objname character varying DEFAULT NULL::character varying, v_objtype character varying DEFAULT NULL::character varying) RETURNS void
    LANGUAGE plpgsql
AS $$
    declare

    -------------------------------------------------------------------------------------
    -- NAME: UTIL_DROP_ANYTHING
    --
    -- Copyright c 2011 Recombinant Data Corp.
    --

    --------------------------------------------------------------------------------------
    v_cmdline varchar(100);


begin

    if upper(v_objtype) like 'TABLE%' then
	v_cmdline := 'drop '|| v_objtype || ' '|| v_objname || ' cascade constraint';
    else
	v_cmdline := 'drop '|| v_objtype || ' '|| v_objname;
    end if;

    begin
	execute v_cmdline;
	raise notice '%%', 'SUCCESS ' ,  v_cmdline;
    exception
	when others then
            raise notice '%%', 'ERROR ' ,  v_cmdline;
    end;

end;

$$;

