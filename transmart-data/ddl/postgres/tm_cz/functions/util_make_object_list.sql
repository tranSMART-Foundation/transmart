--
-- Name: util_make_object_list(character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.util_make_object_list(v_whattype character varying DEFAULT NULL::character varying, OUT v_things character varying) RETURNS character varying
    LANGUAGE plpgsql
AS $$
    declare

    -------------------------------------------------------------------------------------
    -- NAME: UTIL_MAKE_OBJECT_LIST
    --
    -- Copyright c 2011 Recombinant Data Corp.
    --

    --------------------------------------------------------------------------------------


begin

    v_things := replace(upper(v_whattype), 'PROCEDURES', 'P,PC') ;
    v_things := replace(upper(v_things), 'FUNCTION', 'P,PC') ;
    v_things := replace(upper(v_things), 'CONSTRAINTS', 'PK,F') ;
    v_things := replace(upper(v_things), 'CONSTRAINT', 'PK,F') ;
    v_things := replace(upper(v_things), 'FUNCTIONS', 'FN') ;
    v_things := replace(upper(v_things), 'FUNCTION', 'FN') ;
    v_things := replace(upper(v_things), 'TABLES', 'U') ;
    v_things := replace(upper(v_things), 'TABLE', 'U') ;
    v_things := replace(upper(v_things), 'VIEWS', 'V') ;
    v_things := replace(upper(v_things), 'VIEW', 'V') ;

    -- add more common names for things
    -- but now transform a,b into 'a','b'
    v_things := replace(upper(v_things), ',', ''',''') ;
    v_things := '''' || v_things || '''' ;
END;

$$;

