--
-- Name: bio_experiment_uid(character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION bio_experiment_uid(primary_id character varying) RETURNS character varying
    LANGUAGE plpgsql
AS $$
begin
    -- Creates uid for bio_experiment.

    return 'EXP:' || coalesce(primary_ID, 'ERROR');
end;

$$;

