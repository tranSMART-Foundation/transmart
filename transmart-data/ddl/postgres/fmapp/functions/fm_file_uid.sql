--
-- Name: fm_file_uid(character varying); Type: FUNCTION; Schema: fmapp; Owner: -
--
CREATE FUNCTION fm_file_uid(file_id character varying) RETURNS character varying
    LANGUAGE plpgsql
AS $$
begin
    -- Creates uid for fm_file.

    return 'FIL:' || coalesce(file_ID, 'ERROR');
end;
$$;

