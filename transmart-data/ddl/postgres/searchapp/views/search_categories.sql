--
-- Name: search_categories; Type: VIEW; Schema: searchapp; Owner: -
--
CREATE VIEW searchapp.search_categories AS
    SELECT str.child_id AS category_id
	   , st.term_name AS category_name
      FROM searchapp.search_taxonomy_rels str,
	   searchapp.search_taxonomy st
     WHERE ((str.parent_id = (
	 SELECT search_taxonomy_rels.child_id
	   FROM searchapp.search_taxonomy_rels
	  WHERE (search_taxonomy_rels.parent_id IS NULL)))
	  AND (str.child_id = st.term_id));

