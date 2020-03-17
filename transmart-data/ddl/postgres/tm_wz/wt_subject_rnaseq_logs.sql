--
-- Name: wt_subject_rnaseq_logs; Type: TABLE; Schema: tm_wz; Owner: -
--
CREATE TABLE wt_subject_rnaseq_logs
(
  region_id int,
  readcount int,
  assay_id int,
  patient_id int,
  trial_name character varying(100),
  log_readcount int,
  raw_readcount int
);

--
-- Name: wt_subject_rnaseq_logs_i1; Type: INDEX; Schema: tm_wz; Owner: -
--
CREATE INDEX wt_subject_rnaseq_logs_i1 ON wt_subject_rnaseq_logs USING btree (trial_name, region_id);

