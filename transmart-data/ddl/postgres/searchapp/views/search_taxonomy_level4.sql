--
-- Name: search_taxonomy_level4; Type: VIEW; Schema: searchapp; Owner: -
--
CREATE VIEW searchapp.search_taxonomy_level4 AS
    SELECT st.term_id
	   , st.term_name
	   , stl3.category_name
      FROM searchapp.search_taxonomy_rels str,
	   searchapp.search_taxonomy st,
	   searchapp.search_taxonomy_level3 stl3
     WHERE ((str.parent_id = stl3.term_id)
	    AND (str.child_id = st.term_id));

