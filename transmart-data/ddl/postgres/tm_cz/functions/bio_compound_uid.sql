--
-- Name: bio_compound_uid(character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION bio_compound_uid(compound_number character varying) RETURNS character varying
    LANGUAGE plpgsql
AS $$
begin
    -- Function to create compound_uid.

    return 'COM:' || compound_number;
end;

$$;

