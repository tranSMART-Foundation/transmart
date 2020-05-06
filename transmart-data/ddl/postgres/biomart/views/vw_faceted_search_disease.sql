--
-- Name: vw_faceted_search_disease; Type: VIEW; Schema: biomart; Owner: -
--
CREATE OR REPLACE VIEW biomart.vw_faceted_search_disease AS
    SELECT z.bio_assay_analysis_id
	   ,replace(trim(leading '/' from solr_hierarchy),'//','/') AS solr_hierarchy
      FROM (
	  SELECT y.bio_assay_analysis_id
		 ,string_agg((y.path)::text,'/'::text ORDER BY (y,path)::text) AS solr_hierarchy
	    FROM (
		SELECT x.bio_assay_analysis_id
		       , x.top_node, max(x.path) AS path
		  FROM (
		      SELECT DISTINCT bdd.bio_data_id AS bio_assay_analysis_id
				      ,substr(mp.path,2,11) AS top_node
				      ,mp.path AS path
			FROM biomart.bio_data_disease bdd,
			     biomart.bio_disease bd,
			     (SELECT 'DIS:'||ui AS path, ui AS unique_id
				FROM biomart.mesh) mp

		       where bdd.bio_disease_id = bd.bio_disease_id
--and bdd.etl_source like 'TEST%'
			 and bd.mesh_code = mp.unique_id
		  ) x
		 group by x.bio_assay_analysis_id
			  , x.top_node
		 order by x.bio_assay_analysis_id)
		     y
	   group by y.bio_assay_analysis_id)
	       z
     order by bio_assay_analysis_id;

