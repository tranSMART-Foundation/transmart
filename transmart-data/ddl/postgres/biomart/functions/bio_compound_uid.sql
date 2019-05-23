--
-- Name: bio_compound_uid(character varying, character varying, character varying); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION bio_compound_uid(cas_registry character varying, compound_number character varying, cnto_number character varying) RETURNS character varying
    LANGUAGE plpgsql
AS $$
begin
    -- Function to create compound_uid.
    -- uses, in order, cas_registry or compound_registry or cnto_number

    return 'COM:' || coalesce(cas_registry, coalesce(compound_number, coalesce(cnto_number, 'ERROR')));
end;
$$;

