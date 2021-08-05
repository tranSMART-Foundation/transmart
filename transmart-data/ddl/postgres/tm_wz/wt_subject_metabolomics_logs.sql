--
-- Name: wt_subject_metabolomics_logs; Type: TABLE; Schema: tm_wz; Owner: -
--
CREATE TABLE wt_subject_metabolomics_logs (
    probeset character varying(500),
    intensity_value double precision,
    pvalue double precision,
    num_calls int,
    assay_id int,
    patient_id int,
    sample_id int,
    subject_id character varying(100),
    trial_name character varying(100),
    timepoint character varying(250),
    log_intensity double precision
);

--
-- Name: wt_subject_mbolomics_logs_i1; Type: INDEX; Schema: tm_wz; Owner: -
--
CREATE INDEX wt_subject_mbolomics_logs_i1 ON wt_subject_metabolomics_logs USING btree (trial_name, probeset);

