--
-- Name: util_drop_synonym(character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.util_drop_synonym(v_objname character varying DEFAULT NULL::character varying) RETURNS void
    LANGUAGE plpgsql
AS $$
    declare

    -------------------------------------------------------------------------------------
    -- NAME: UTIL_DROP_SYNONYM
    --
    -- Copyright c 2011 Recombinant Data Corp.
    --

    --------------------------------------------------------------------------------------
    v_cmdline varchar(100);

    ts CURSOR FOR
		  SELECT 'drop synonym ' || synonym_name || ' ' from user_synonyms;



begin

    open ts;
    fetchl ts into v_cmdline;
    while ts%FOUND
	loop

	begin
            begin

		begin
		    execute v_cmdline;
		    raise notice '%%', 'SUCCESS ' ,  v_cmdline;
		end;
            exception
		when others then

		    begin
			raise notice '%%', 'ERROR ' ,  v_cmdline;
		    end;
            end;
            fetch ts into v_cmdline;
	end;
    end loop;
    close ts;
end;

$$;
