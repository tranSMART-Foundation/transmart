--
-- Name: isnumeric(character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.isnumeric(p_string character varying) RETURNS numeric
    LANGUAGE plpgsql
AS $$
    declare

    l_number numeric;

begin
    l_number := p_string;
    return 1;

exception
    when others then
        return 0;

end;

$$;

