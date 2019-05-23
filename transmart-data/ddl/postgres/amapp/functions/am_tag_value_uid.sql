--
-- Name: am_tag_value_uid(bigint); Type: FUNCTION; Schema: amapp; Owner: -
--
CREATE OR REPLACE FUNCTION am_tag_value_uid(tag_value_id bigint) RETURNS character varying
    LANGUAGE plpgsql
AS $$
begin
    RETURN 'TAG:' || TAG_VALUE_ID::text;
end;
$$;

