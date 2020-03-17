--
-- Name: dropsyn(); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.dropsyn() RETURNS void
    LANGUAGE plpgsql
AS $$

    -- Attention:
    -- Oracle procedure
    -- Rewrite for permissions for postgresql

    declare

    s_cur cursor for
		     select synonym_name
		     from pg_tables;

    RetVal  bigint;
    sqlstr  varchar(200);

begin
    for s_rec in s_cur loop
	sqlstr := 'DROP SYNONYM ' || s_rec.synonym_name;

	execute sqlstr;
	commit;
    end loop;
end;

$$;

