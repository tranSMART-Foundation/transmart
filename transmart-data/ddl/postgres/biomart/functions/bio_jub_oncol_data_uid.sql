--
-- Name: bio_jub_oncol_data_uid(numeric, character varying); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION bio_jub_oncol_data_uid(record_id numeric, bio_curation_name character varying) RETURNS character varying
    LANGUAGE plpgsql
AS $$
begin
    -- Creates uid for bio_jub_oncol_data.

    return 'BJOD:' || coalesce(trim(to_char(record_id, '9999999999999999999')), 'ERROR') || ':' || coalesce(bio_curation_name, 'ERROR');
end;
$$;

