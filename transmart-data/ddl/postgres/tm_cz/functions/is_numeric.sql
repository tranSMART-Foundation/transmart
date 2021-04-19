--
-- Name: is_numeric(character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.is_numeric(character varying) RETURNS numeric
    LANGUAGE plpgsql IMMUTABLE STRICT
AS $$
    declare

    i numeric;

begin

    i := $1::numeric;
    return 0;

exception when invalid_text_representation then
              return 1;
end;
$$;

