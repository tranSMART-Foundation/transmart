--
-- Name: util_truncate_table(character varying, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.util_truncate_table(v_tabname character varying DEFAULT NULL::character varying, v_dummyarg character varying DEFAULT 'Y'::character varying) RETURNS void
    LANGUAGE plpgsql
AS $$
    declare

    -------------------------------------------------------------------------------------
    -- NAME: UTIL_TRUNCATE_TABLE
    --
    -- Copyright c 2017 TranSMART Foundation
    --

    --------------------------------------------------------------------------------------
begin

    execute 'truncate table ' || v_tabname || ' cascade';

end;

$$;

