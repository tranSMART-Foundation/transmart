--
-- Name: tmp_analysis_eqtl_top500; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE tmp_analysis_eqtl_top500 (
    bio_asy_analysis_eqtl_id int NOT NULL,
    bio_assay_analysis_id int NOT NULL,
    rs_id character varying(50),
    p_value double precision,
    log_p_value double precision,
    etl_id int,
    ext_data character varying(4000),
    p_value_char character varying(100),
    gene character varying(50),
    cis_trans character varying(10),
    distance_from_gene character varying(10),
    rnum int
);
--
-- Name: t_a_gae_t500_idx; Type: INDEX; Schema: biomart; Owner: -
--
CREATE INDEX t_a_gae_t500_idx ON tmp_analysis_eqtl_top500 USING btree (bio_assay_analysis_id);
--
-- Name: t_a_ge_t500_idx; Type: INDEX; Schema: biomart; Owner: -
--
CREATE INDEX t_a_ge_t500_idx ON tmp_analysis_eqtl_top500 USING btree (rs_id);
