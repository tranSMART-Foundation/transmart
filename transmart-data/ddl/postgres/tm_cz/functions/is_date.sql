--
-- Name: is_date(character varying, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.is_date(character varying, character varying) RETURNS numeric
    LANGUAGE plpgsql IMMUTABLE STRICT
AS $$

    declare

    i numeric;

begin

    i := $1::date;
    return 0;

exception when invalid_text_representation then
              return 1;
end;
$$;

