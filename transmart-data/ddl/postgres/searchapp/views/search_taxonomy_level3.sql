--
-- Name: search_taxonomy_level3; Type: VIEW; Schema: searchapp; Owner: -
--
CREATE VIEW searchapp.search_taxonomy_level3 AS
    SELECT st.term_id
	   , st.term_name
	   , stl2.category_name
      FROM searchapp.search_taxonomy_rels str,
	   searchapp.search_taxonomy st,
	   searchapp.search_taxonomy_level2 stl2
     WHERE ((str.parent_id = stl2.term_id)
	    AND (str.child_id = st.term_id));

