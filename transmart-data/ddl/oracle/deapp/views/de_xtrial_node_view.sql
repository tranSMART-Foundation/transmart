--
-- Type: VIEW; Owner: DEAPP; Name: DE_XTRIAL_NODE_VIEW
--
CREATE OR REPLACE FORCE VIEW "DEAPP"."DE_XTRIAL_NODE_VIEW" ("MODIFIER_PATH", "MODIFIER_CD", "NAME_CHAR", "MODIFIER_BLOB", "UPDATE_DATE", "DOWNLOAD_DATE", "IMPORT_DATE", "SOURCESYSTEM_CD", "UPLOAD_ID", "VALTYPE_CD", "STD_UNITS", "VISIT_IND") AS 
SELECT xn.modifier_path,
  xn.modifier_cd,
  xn.name_char,
  xn.modifier_blob,
  xn.update_date,
  xn.download_date,
  xn.import_date,
  xn.sourcesystem_cd,
  xn.upload_id,
  xm.valtype_cd,
  xm.std_units,
  xm.visit_ind
 FROM deapp.de_xtrial_node xn
   LEFT JOIN deapp.de_xtrial_metadata xm ON xn.modifier_cd = xm.modifier_cd;

