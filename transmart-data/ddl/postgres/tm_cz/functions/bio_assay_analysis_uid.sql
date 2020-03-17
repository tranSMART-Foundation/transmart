--
-- Name: bio_assay_analysis_uid(character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.bio_assay_analysis_uid(analysis_name character varying) RETURNS character varying
    LANGUAGE plpgsql
AS $$
begin
    -- Creates uid for bio_experiment.

    return 'BAA:' || coalesce(analysis_name, 'ERROR');
end;

$$;

--
-- Name: bio_assay_analysis_uid(bigint); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.bio_assay_analysis_uid(analysis_id bigint) RETURNS character varying
    LANGUAGE plpgsql
AS $$
begin

    -- Creates uid for bio_assay_analysis.

    return 'BAA:' || coalesce(analysis_id, -1);
end;

$$;

