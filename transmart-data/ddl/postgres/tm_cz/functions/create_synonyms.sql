--
-- Name: create_synonyms(character varying, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.create_synonyms(fromdb character varying, todb character varying) RETURNS void
    LANGUAGE plpgsql
AS $$
    -- Attention:
    -- Oracle procedure
    -- synonyms not needed on postgresql
    -- any related issues we can address e.g. permissions?

    declare

    cTableList cursor for
			  select
			  tableschema
			  ,tablename
			  from
			  pg_tables
			  order by tableschema
			  , tablename;

    dbCount bigint;
    sourceDB varchar(200);
    targetDB varchar(200);

    dynamicSQL varchar(2000);


BEGIN
    -------------------------------------------------------------------------------
    --Create or replace Synonyms Point to DB A (TO) From DB B (FROM)
    --Input: From DB, TO DB
    --Output: Nothing
    -- KCR@20090310 - First rev.
    -------------------------------------------------------------------------------

    /* CANT READ FROM DBA_TABLESPACES
       --Check that DB's exist
       select count(*) into dbCount from dba_tablespaces where tablespace_name = upper(fromDB);
       if dbCOunt > 1
       then
       dbms_output.put_line('From DB is invalid!: ' || fromDB);
       end if;

       if dbCount > 1
       then
       dbms_output.put_line('TO DB is invalid!: ' || toDB);
       end if;
     */

    sourceDB := lower(fromDB);
    targetDB := lower(toDB);



    --Loop through full list of results (All table for all schemas)
    for r_cTableList in cTableList
	loop
	--if The current owner(DB) matched the toDB then begin creating Synonyms.
	if r_cTableList.tableschema = lower(targetDB) then
            dynamicSQL := 'create or replace  or replace synonym "' || sourceDB || '"."' || r_cTableList.tablename || '" FOR "' || targetDB || '"."' || r_cTableList.tablename || '"';
            raise notice '%', dynamicSQL;
            execute dynamicSQL;
	    end if;
	commit;
    end loop; --Loops through full resultset

end;

$$;

