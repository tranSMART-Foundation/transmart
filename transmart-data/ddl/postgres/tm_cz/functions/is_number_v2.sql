--
-- Name: is_number_v2(character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.is_number_v2(p_string character varying) RETURNS numeric
    LANGUAGE plpgsql
AS $$
    declare

    l_number numeric;

begin
    l_number := p_string;
    return l_number;

exception
    when others then
        return 1;

end;

$$;

