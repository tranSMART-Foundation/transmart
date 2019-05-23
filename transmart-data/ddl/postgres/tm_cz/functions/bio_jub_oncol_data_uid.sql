--
-- Name: bio_jub_oncol_data_uid(numeric, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION bio_jub_oncol_data_uid(record_id numeric, bio_curation_name character varying) RETURNS character varying
    LANGUAGE plpgsql
AS $$
begin
    -- Creates uid for bio_jub_oncol_data.

    return 'BJOD:' || coalesce(to_char(record_id), 'ERROR') || ':' || coalesce(bio_curation_name, 'ERROR');
end;

$$;

