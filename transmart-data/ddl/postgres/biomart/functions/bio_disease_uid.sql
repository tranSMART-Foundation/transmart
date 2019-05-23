--
-- Name: bio_disease_uid(character varying); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION bio_disease_uid(mesh_code character varying) RETURNS character varying
    LANGUAGE plpgsql
AS $$
begin
    -- Creates bio_disease_uid.

    return 'DIS:' || coalesce(mesh_code, 'ERROR');
end;
$$;

