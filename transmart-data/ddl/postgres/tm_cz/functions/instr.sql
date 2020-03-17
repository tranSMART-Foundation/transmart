--
-- Name: instr(character varying, character varying, integer, integer); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.instr(string character varying, string_to_search character varying, beg_index integer DEFAULT 1, occur_index integer DEFAULT 1) RETURNS integer
    LANGUAGE plpgsql IMMUTABLE STRICT
AS $$
    declare

    pos integer NOT NULL DEFAULT 0;
    occur_number integer NOT NULL DEFAULT 0;
    temp_str varchar;
    beg integer;
    i integer;
    length integer;
    ss_length integer;

begin
    if beg_index > 0 then
        beg := beg_index;
        temp_str := substring(string from beg_index);

        for i in 1..occur_index loop
            pos := position(string_to_search in temp_str);

            if i = 1 then
                beg := beg + pos - 1;
            else
                beg := beg + pos;
            end if;

            temp_str := substring(string from beg + 1);
        end loop;

        if pos = 0 then
            return 0;
        else
            return beg;
        end if;
    else
        ss_length := char_length(string_to_search);
        length := char_length(string);
        beg := length + beg_index - ss_length + 2;

        while beg > 0 loop
            temp_str := substring(string from beg for ss_length);
            pos := position(string_to_search in temp_str);

            if pos > 0 then
                occur_number := occur_number + 1;

                if occur_number = occur_index then
                    return beg;
                end if;
            end if;

            beg := beg - 1;
        end loop;

        return 0;
    end if;
end;
$$;

