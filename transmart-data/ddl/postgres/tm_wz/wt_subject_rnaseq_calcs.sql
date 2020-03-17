--
-- Name: wt_subject_rnaseq_calcs; Type: TABLE; Schema: tm_wz; Owner: -
--
CREATE TABLE wt_subject_rnaseq_calcs
(
  --WL--trial_name character varying(100),
  region_id int,
  mean_readcount double precision,
  median_readcount double precision,
  stddev_readcount double precision
);

--
-- Name: wt_subject_rnaseq_calcs_i1; Type: INDEX; Schema: tm_wz; Owner: -
--
CREATE INDEX wt_subject_rnaseq_calcs_i1 ON wt_subject_rnaseq_calcs USING btree (region_id);

