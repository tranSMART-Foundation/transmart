--
-- Name: bio_metab_subpathway_view; Type: VIEW; Schema: biomart; Owner: -
--
CREATE VIEW biomart.bio_metab_subpathway_view AS
    SELECT sp.id AS subpathway_id
    	   , b.bio_marker_id AS asso_bio_marker_id
	   , 'SUBPATHWAY TO METABOLITE'::text AS correl_type
      FROM (((deapp.de_metabolite_sub_pathways sp
	      JOIN deapp.de_metabolite_sub_pway_metab j
		      ON ((sp.id = j.sub_pathway_id)))
	      JOIN deapp.de_metabolite_annotation m
		      ON ((m.id = j.metabolite_id)))
	      JOIN bio_marker b
		      ON ((((b.bio_marker_type)::text = 'METABOLITE'::text)
			   AND ((b.primary_external_id)::text = (m.hmdb_id)::text))));

--
-- Name: bio_metab_superpathway_view; Type: VIEW; Schema: biomart; Owner: -
--
CREATE VIEW biomart.bio_metab_superpathway_view AS
    SELECT supp.id AS superpathway_id, b.bio_marker_id AS asso_bio_marker_id
	   , 'SUPERPATHWAY TO METABOLITE'::text AS correl_type
      FROM ((((deapp.de_metabolite_super_pathways supp
	       JOIN deapp.de_metabolite_sub_pathways subp
		       ON ((supp.id = subp.super_pathway_id)))
	       JOIN deapp.de_metabolite_sub_pway_metab j
		       ON ((subp.id = j.sub_pathway_id)))
	       JOIN deapp.de_metabolite_annotation m
		       ON ((m.id = j.metabolite_id)))
	       JOIN bio_marker b
		       ON ((((b.bio_marker_type)::text = 'METABOLITE'::text)
			    AND ((b.primary_external_id)::text = (m.hmdb_id)::text))));

--
-- Name: tf_trg_bio_analysis_att_baal(); Type: FUNCTION; Schema: biomart; Owner: -
--
CREATE FUNCTION tf_trg_bio_analysis_att_baal() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    case TG_OP
    when ' INSERT' then
	-- create a new record in the lineage table for each ancestor of this term (including self)
	insert into bio_analysis_attribute_lineage
	(bio_analysis_attribute_id, ancestor_term_id, ancestor_search_keyword_id)
	select :NEW.bio_analysis_attribute_id, skl.ancestor_id, skl.search_keyword_id
	from searchapp.solr_keywords_lineage skl
	where skl.term_id = :NEW.term_id;
    else
	RAISE EXCEPTION 'This trigger function expects only INSERT';  
    end case;

    end if;
end;
$$;

--
-- Name: trg_bio_analysis_att_baal; Type: TRIGGER; Schema: biomart; Owner: -
--
CREATE TRIGGER trg_bio_analysis_att_baal AFTER INSERT ON bio_analysis_attribute FOR EACH ROW EXECUTE PROCEDURE tf_trg_bio_analysis_att_baal();
