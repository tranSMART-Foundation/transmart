--
-- Name: drop_table(character varying, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION drop_table(tabowner character varying, tabname character varying) RETURNS void
    LANGUAGE plpgsql
AS $$
    DECLARE

    temp integer:=0;
    drp_stmt varchar(200):=null;

    
BEGIN
    select count(*) into
        temp
      from pg_tables
     where tablename = lower(tabname)
    and
        tableschema = lower(tabowner);

    if temp = 1 then
        drp_stmt := 'Drop Table ' || lower(tabowner) || '.' || lower(tabname);
        execute drp_stmt;
        commit;
    end if;

exception
    when others then
	raise exception 'An error was encountered - % -ERROR- %',SQLSTATE,SQLERRM;

END;

$$;

