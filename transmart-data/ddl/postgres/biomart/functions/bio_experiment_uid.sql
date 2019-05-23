--
-- Name: bio_experiment_uid(character varying); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION bio_experiment_uid(primary_id character varying) RETURNS character varying
    LANGUAGE plpgsql
AS $$
begin
    -- Creates uid for bio_experiment.

    return 'EXP:' || coalesce(primary_id, 'ERROR');
end;
$$;

