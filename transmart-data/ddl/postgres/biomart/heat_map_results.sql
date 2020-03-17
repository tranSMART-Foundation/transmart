--
-- Name: heat_map_results; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE heat_map_results (
    subject_id character varying(100),
    log_intensity double precision,
    cohort_id character varying(255),
    probe_id character varying(100),
    bio_assay_feature_group_id int,
    fold_change_ratio double precision,
    tea_normalized_pvalue double precision,
    bio_marker_name character varying(400),
    bio_marker_id int,
    search_keyword_id int,
    bio_assay_analysis_id int,
    trial_name character varying(100),
    significant int,
    gene_id character varying(100),
    assay_id int,
    preferred_pvalue double precision
);

