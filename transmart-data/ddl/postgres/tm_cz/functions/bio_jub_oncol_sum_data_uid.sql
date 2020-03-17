--
-- Name: bio_jub_oncol_sum_data_uid(numeric, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.bio_jub_oncol_sum_data_uid(record_id numeric, bio_curation_name character varying) RETURNS character varying
    LANGUAGE plpgsql
AS $$
begin
    -- Creates uid for bio_jub_oncol_sum_data.

    return 'BJOS:' || coalesce(to_char(record_id), 'ERROR') || ':' || coalesce(bio_curation_name, 'ERROR');
end;

$$;

