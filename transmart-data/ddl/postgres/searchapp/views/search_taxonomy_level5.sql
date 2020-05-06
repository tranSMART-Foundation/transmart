--
-- Name: search_taxonomy_level5; Type: VIEW; Schema: searchapp; Owner: -
--
CREATE VIEW searchapp.search_taxonomy_level5 AS
    SELECT st.term_id
	   , st.term_name
	   , stl4.category_name
      FROM searchapp.search_taxonomy_rels str,
	   searchapp.search_taxonomy st,
	   searchapp.search_taxonomy_level4 stl4
     WHERE ((str.parent_id = stl4.term_id)
	    AND (str.child_id = st.term_id));

