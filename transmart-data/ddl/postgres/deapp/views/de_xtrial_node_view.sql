--
-- Name: de_xtrial_node_view; Type: VIEW; Schema: deapp; Owner: -
--
CREATE VIEW deapp.de_xtrial_node_view AS
 SELECT xn.modifier_path
	, xn.modifier_cd
	, xn.name_char
	, xn.modifier_blob
	, xn.update_date
	, xn.download_date
	, xn.import_date
	, xn.sourcesystem_cd
	, xn.upload_id
	, xn.modifier_level
	, xn.modifier_node_type
	, xm.valtype_cd
	, xm.std_units
	, xm.visit_ind
   FROM (deapp.de_xtrial_node xn
	 LEFT JOIN deapp.de_xtrial_metadata xm
		 ON (((xn.modifier_cd)::text = (xm.modifier_cd)::text)));

