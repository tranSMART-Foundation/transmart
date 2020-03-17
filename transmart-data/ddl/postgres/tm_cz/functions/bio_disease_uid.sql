--
-- Name: bio_disease_uid(character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.bio_disease_uid(mesh_code character varying) RETURNS character varying
    LANGUAGE plpgsql
AS $$
begin
    -- Creates bio_disease_uid.

    return 'DIS:' || coalesce(mesh_code, 'ERROR');
end;

$$;

