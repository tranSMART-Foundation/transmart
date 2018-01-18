--
-- Name: util_truncate_table(character varying, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION util_truncate_table(v_tabname character varying DEFAULT NULL::character varying, v_dummyarg character varying DEFAULT 'Y'::character varying) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE

-------------------------------------------------------------------------------------
-- NAME: UTIL_TRUNCATE_TABLE
--
-- Copyright c 2017 TranSMART Foundation
--

--------------------------------------------------------------------------------------
begin

execute 'truncate table ' || v_tabname || ' cascade';

END;
 
$$;

