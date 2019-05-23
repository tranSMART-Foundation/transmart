--
-- Name: bio_assay_platform_uid(character varying); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION bio_assay_platform_uid(platform_name character varying) RETURNS character varying
    LANGUAGE plpgsql
AS $$
begin
    -- Creates uid for bio_assay_platform

    return 'BAP:' || coalesce(platform_name, 'ERROR');
end;
$$;

