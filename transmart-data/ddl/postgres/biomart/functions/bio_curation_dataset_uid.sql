--
-- Name: bio_curation_dataset_uid(character varying); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION bio_curation_dataset_uid(bio_curation_type character varying) RETURNS character varying
    LANGUAGE plpgsql
AS $$
begin
    -- Creates uid for bio_curation_dataset.

    return 'BCD:' || coalesce(bio_curation_type, 'ERROR');
end;
$$;

