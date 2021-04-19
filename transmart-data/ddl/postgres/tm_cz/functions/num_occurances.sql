--
-- Name: num_occurances(character varying, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.num_occurances(input_str character varying, search_str character varying) RETURNS integer
    LANGUAGE plpgsql
AS $$
    declare

    num integer;

begin
    num := 0;
    while tm_cz.instr(input_str, search_str, 1, num + 1) > 0 loop
	num := num + 1;
    end loop;

    return num;
end;

$$;

