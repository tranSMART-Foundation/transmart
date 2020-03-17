--
-- Name: bio_asy_analysis_pltfm_uid(character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.bio_asy_analysis_pltfm_uid(platform_name character varying) RETURNS character varying
    LANGUAGE plpgsql
AS $$
begin
    -- Creates uid for bio_asy_analysis_pltfm.

    return 'BAAP:' || coalesce(platform_name, 'ERROR');
end;

$$;

