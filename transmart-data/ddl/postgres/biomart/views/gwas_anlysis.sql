--
-- Name: gwas_anlysis; Type: VIEW; Schema: biomart; Owner: -
--
CREATE VIEW biomart.gwas_anlysis AS
   SELECT  bio_assay_analysis_id::character varying(20) bio_assay_analysis_id, etl_id, analysis_name, bio_assay_data_type
   FROM biomart.bio_assay_analysis
   WHERE bio_assay_data_type not in ('Gene Expression');
