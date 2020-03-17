--
-- Name: biomarker_pathway_uid(character varying, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.biomarker_pathway_uid(p_source character varying, pathway_id character varying) RETURNS character varying
    LANGUAGE plpgsql
AS $$
begin
    -- Creates uid for biomarker_pathway.

    return 'PATHWAY:'|| p_source || ':' || coalesce(pathway_id, 'ERROR');
end;

$$;

